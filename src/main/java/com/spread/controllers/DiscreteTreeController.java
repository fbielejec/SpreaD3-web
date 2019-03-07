package com.spread.controllers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.GsonBuilder;
import com.spread.data.Attribute;
import com.spread.data.AxisAttributes;
import com.spread.data.Layer;
import com.spread.data.Location;
import com.spread.data.SpreadData;
import com.spread.data.TimeLine;
import com.spread.data.attributable.Line;
import com.spread.data.attributable.Point;
import com.spread.data.geojson.GeoJsonData;
import com.spread.data.primitive.Coordinate;
import com.spread.domain.DiscreteAttributeEntity;
import com.spread.domain.DiscreteTreeModelEntity;
import com.spread.exceptions.SpreadException;
import com.spread.loggers.AbstractLogger;
import com.spread.loggers.ILogger;
import com.spread.parsers.DiscreteLocationsParser;
import com.spread.parsers.DiscreteTreeParser;
import com.spread.parsers.GeoJSONParser;
import com.spread.parsers.TimeParser;
import com.spread.repositories.DiscreteTreeModelRepository;
import com.spread.repositories.KeyRepository;
import com.spread.services.ipfs.IpfsService;
import com.spread.services.storage.StorageService;
import com.spread.services.visualization.VisualizationService;
import com.spread.utils.ParsersUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.jsonwebtoken.SignatureException;
import jebl.evolution.io.ImportException;
import jebl.evolution.trees.RootedTree;

@Controller
@CrossOrigin
@Transactional
@RequestMapping("/discrete")
public class DiscreteTreeController {

    private AbstractLogger logger;

    @Autowired
    private IpfsService ipfsService;

    @Autowired
    private DiscreteTreeModelRepository modelRepository;

    @Autowired
    private KeyRepository keyRepository;

    @Autowired
    @Qualifier("longRunningTaskExecutor")
    private Executor longRunningTaskExecutor;

    @Autowired
    private VisualizationService visualizationService;

    @Autowired
    private StorageService storageService;

    public DiscreteTreeController() {
    }

    public void init(AbstractLogger logger) {
        this.logger = logger;
    }

    /*
     * This path creates the entity
     * Reuploading tree file with the same sessionId is not possible
     * clients carrying the token should first delete the entity by calling the corresponding endpoint and ask for a new token.
     * */
    @RequestMapping(path = "/tree", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadTree(HttpServletRequest request,
                                             @RequestHeader(value = "Authorization") String authorizationHeader,
                                             @RequestParam(value = "file", required = true) MultipartFile file) {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            if(!(modelRepository.findBySessionId(sessionId) == null)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session with that id already exists.");
            };

            String filename = file.getOriginalFilename();

            storageService.createSubdirectory(sessionId);
            storageService.store(sessionId, file);

            DiscreteTreeModelEntity model = new DiscreteTreeModelEntity(sessionId, storageService.loadAsResource(sessionId, filename).getFile().getAbsolutePath());

            RootedTree tree = ParsersUtils.importRootedTree(model.getTreeFilename ());

            Set<DiscreteAttributeEntity> attributes = tree.getNodes().stream().filter(node -> !tree.isRoot(node))
                .flatMap(node -> node.getAttributeNames().stream()).map(name -> {
                        return new DiscreteAttributeEntity(name, model);
                    }).collect(Collectors.toSet());

            model.setAttributes(attributes);
            modelRepository.save(model);

            logger.log(ILogger.INFO, "Tree file successfully persisted", new String[][] {
                    {"sessionId", sessionId},
                    {"filename", filename},
                    {"numberOfAttributes", String.valueOf(attributes.size())},
                    {"request-ip" , request.getRemoteAddr()},
                });

            return ResponseEntity.status(HttpStatus.CREATED).header("Location", sessionId).body(ControllerUtils.jsonResponse("OK"));
        } catch (SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Authorication", "Bearer").body(ControllerUtils.jsonResponse(e.getMessage()));
            } catch (IOException | ImportException e) {
                String message = Optional.ofNullable(e.getMessage()).orElse("Exception encountered when importing tree file");
                logger.log(ILogger.ERROR, e, new String[][] {
                        {"message", message},
                        {"sessionId", sessionId},
                    });
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ControllerUtils.jsonResponse("INTERNAL SERVER ERROR"));
        } catch (SpreadException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                },
                e.getMeta());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Authorication", "Bearer").body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        }
    }


// TODO : locations endpoint













    private String doGenerateOutput (DiscreteTreeModelEntity model) throws SpreadException, IOException, ImportException {

        TimeLine timeLine = null;
        LinkedList<Attribute> mapAttributes = null;
        LinkedList<Attribute> lineAttributes = null;
        LinkedList<Attribute> pointAttributes = null;
        LinkedList<Location> locationsList = null;

        LinkedList<Layer> layersList = new LinkedList<Layer>();

        // ---IMPORT---//

        RootedTree rootedTree = ParsersUtils.importRootedTree(model.getTreeFilename());

        System.out.println("Imported tree");

        // TODO : remove file use, Locations colection instead
        DiscreteLocationsParser locationsParser = new DiscreteLocationsParser(model.getLocationsFilename(), false
                                                                              // model.hasHeader()
                                                                              // settings.locationsFilename, settings.hasHeader
                                                                              );
        locationsList = locationsParser.parseLocations();

        System.out.println("Imported locations");

        // ---PARSE AND FILL STRUCTURES---//

        // ---TIME---//

        TimeParser timeParser = new TimeParser(model.getMrsd ());
        timeLine = timeParser.getTimeLine(rootedTree.getHeight(rootedTree
                                                               .getRootNode()));

        System.out.println("Parsed time line");

        // ---GEOJSON LAYER---//

        if (model.getGeojsonFilename() != null) {

            GeoJSONParser geojsonParser = new GeoJSONParser(model.getGeojsonFilename ());
            GeoJsonData geojson = geojsonParser.parseGeoJSON();

            mapAttributes = geojsonParser.getUniqueMapAttributes();

            String geojsonLayerId = ParsersUtils.splitString(model.getGeojsonFilename (),
                                                             "/");
            Layer geojsonLayer = new Layer(geojsonLayerId, //
                                           "GeoJson layer", //
                                           geojson);

            layersList.add(geojsonLayer);

            System.out.println("Parsed map attributes");

        }// END: null check

        // ---DATA LAYER (POINTS WITH COUNTS)---//

        DiscreteTreeParser treeParser = new DiscreteTreeParser(
                                                               rootedTree,
                                                               model.getLocationAttribute (),
                                                               locationsList,
                                                               timeParser,
                                                               model.getTimescaleMultiplier ()
                                                               );
        treeParser.parseTree();

        LinkedList<Point> countsList = treeParser.getCountsList();

        String countsLayerId = ParsersUtils.splitString(model.getTreeFilename (), "/");
        Layer countsLayer = new Layer(countsLayerId, //
                                      "Counts layer", //
                                      countsList //
                                      );

        layersList.add(countsLayer);

        System.out.println("Parsed counts");

        // ---DATA LAYER (TREE LINES & POINTS WITH LOCATIONS)---//

        LinkedList<Line> linesList = treeParser.getLinesList();
        LinkedList<Point> pointsList = treeParser.getPointsList();

        String treeLayerId = ParsersUtils.splitString(model.getTreeFilename (), "/");
        Layer treeLayer = new Layer(treeLayerId, //
                                    "Tree layer", //
                                    pointsList, //
                                    linesList //
                                    );

        layersList.add(treeLayer);

        System.out.println("Parsed lines and points");

        lineAttributes = treeParser.getLineAttributes();
        pointAttributes = treeParser.getPointAttributes();

        LinkedList<Attribute> rangeAttributes = getCoordinateRangeAttributes(locationsList);
        Attribute xCoordinate = rangeAttributes.get(ParsersUtils.X_INDEX);
        Attribute yCoordinate = rangeAttributes.get(ParsersUtils.Y_INDEX);

        pointAttributes.add(xCoordinate);
        pointAttributes.add(yCoordinate);
        AxisAttributes axis = new AxisAttributes(xCoordinate.getId(),
                                                 yCoordinate.getId());

        System.out.println("Parsed tree attributes");

        SpreadData data = new SpreadData(timeLine,
                                         axis,
                                         mapAttributes,
                                         lineAttributes,
                                         pointAttributes,
                                         null, // areaAttributes
                                         locationsList,
                                         layersList
                                         );

        return new GsonBuilder().create().toJson(data);
    }

    private LinkedList<Attribute> getCoordinateRangeAttributes(LinkedList<Location> locationsList) throws SpreadException  {

        LinkedList<Attribute> coordinateRange = new LinkedList<Attribute>();

        Double[] xCoordinateRange = new Double[2];
        xCoordinateRange[Attribute.MIN_INDEX] = Double.MAX_VALUE;
        xCoordinateRange[Attribute.MAX_INDEX] = Double.MIN_VALUE;

        Double[] yCoordinateRange = new Double[2];
        yCoordinateRange[Attribute.MIN_INDEX] = Double.MAX_VALUE;
        yCoordinateRange[Attribute.MAX_INDEX] = Double.MIN_VALUE;

        for (Location location : locationsList) {

            Coordinate coordinate = location.getCoordinate();
            if (coordinate == null) {
                throw new SpreadException("Location " + location.getId()
                                          + " has no coordinates set.");
            }

            Double latitude = coordinate.getYCoordinate();
            Double longitude = coordinate.getXCoordinate();

            // update coordinates range

            if (latitude < yCoordinateRange[Attribute.MIN_INDEX]) {
                yCoordinateRange[Attribute.MIN_INDEX] = latitude;
            }

            if (latitude > yCoordinateRange[Attribute.MAX_INDEX]) {
                yCoordinateRange[Attribute.MAX_INDEX] = latitude;
            }

            if (longitude < xCoordinateRange[Attribute.MIN_INDEX]) {
                xCoordinateRange[Attribute.MIN_INDEX] = longitude;
            }

            if (longitude > xCoordinateRange[Attribute.MAX_INDEX]) {
                xCoordinateRange[Attribute.MAX_INDEX] = longitude;
            }

        }

        Attribute xCoordinate = new Attribute("xCoordinate", xCoordinateRange);
        Attribute yCoordinate = new Attribute("yCoordinate", yCoordinateRange);

        coordinateRange.add(ParsersUtils.X_INDEX, xCoordinate);
        coordinateRange.add(ParsersUtils.Y_INDEX, yCoordinate);

        return coordinateRange;
    }








}
