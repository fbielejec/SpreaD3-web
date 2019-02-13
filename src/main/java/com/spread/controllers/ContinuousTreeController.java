package com.spread.controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import com.google.gson.GsonBuilder;
import com.spread.data.Attribute;
import com.spread.data.AxisAttributes;
import com.spread.data.Layer;
import com.spread.data.SpreadData;
import com.spread.data.TimeLine;
import com.spread.data.attributable.Area;
import com.spread.data.attributable.Line;
import com.spread.data.attributable.Point;
import com.spread.data.geojson.GeoJsonData;
import com.spread.domain.AttributeEntity;
import com.spread.domain.ContinuousTreeModelEntity;
import com.spread.domain.SessionEntity;
import com.spread.exceptions.SpreadException;
import com.spread.loggers.ILogger;
import com.spread.loggers.LoggerFactory;
import com.spread.parsers.ContinuousTreeParser;
import com.spread.parsers.GeoJSONParser;
import com.spread.parsers.TimeParser;
import com.spread.repositories.ContinuousTreeModelRepository;
import com.spread.repositories.KeyRepository;
import com.spread.services.ipfs.IpfsService;
import com.spread.services.storage.StorageService;
import com.spread.services.visualization.VisualizationService;
import com.spread.utils.TokenUtils;
import com.spread.utils.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
@RequestMapping("/continuous")
public class ContinuousTreeController {

    private final ILogger logger;
    private final StorageService storageService;

    @Autowired
    private IpfsService ipfsService;

    @Autowired
    private ContinuousTreeModelRepository modelRepository;

    @Autowired
    private KeyRepository keyRepository;

    @Autowired
    private Executor taskExecutor;

    @Autowired
    private VisualizationService visualizationService;

    public ContinuousTreeController(StorageService storageService) {
        this.logger = new LoggerFactory().getLogger(LoggerFactory.DEFAULT);
        this.storageService = storageService;
    }

    /*
     * This path creates the entity
     * Reuploading tree file with the same sessionId is not possible
     * clients carrying the token should first delete the entity by calling the corresponding endpoint and ask for a new token.
     * */
    @RequestMapping(path = "/tree", method = RequestMethod.POST)
    public ResponseEntity<String> uploadTree(@RequestHeader(value = "Authorization") String authorizationHeader,
                                             @RequestParam(value = "treefile", required = true) MultipartFile file) {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            if(!(modelRepository.findBySessionId(sessionId) == null)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session with that id already exists.");
            };
            // reaupload not possible, just redo the analysis
            //			if (storageService.exists(sessionId, file)) {
            //				storageService.delete(sessionId, filename);
            //				logger.log("Deleting previously uploaded tree file: " + filename, ILogger.INFO);
            //			}

            String filename = file.getOriginalFilename();

            storageService.createSubdirectory(sessionId);
            storageService.store(sessionId, file);
            logger.log("tree file " + filename + " successfully persisted.", ILogger.INFO);

            ContinuousTreeModelEntity continuousTreeModel = new ContinuousTreeModelEntity(storageService.loadAsResource(sessionId, filename).getFile().getAbsolutePath(),
                                                                                          new SessionEntity(sessionId));

            RootedTree tree = Utils.importRootedTree(continuousTreeModel.getTreeFilename());

            Set<AttributeEntity> atts = tree.getNodes().stream().filter(node -> !tree.isRoot(node))
                .flatMap(node -> node.getAttributeNames().stream()).map(name -> {
                        return new AttributeEntity(name, continuousTreeModel);
                    }).collect(Collectors.toSet());

            continuousTreeModel.setAttributes(atts);

            modelRepository.save(continuousTreeModel);

            logger.log("continuousTreeModelEntity with id " + continuousTreeModel.getId() + " successfully persisted.",
                       ILogger.INFO);

            logger.log(atts.size() + " attributes successfully persisted.", ILogger.INFO);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", sessionId);
            return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
        } catch (IOException | ImportException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(path = "/tree", method = RequestMethod.DELETE)
    public ResponseEntity<Object> deleteTree(@RequestHeader(value = "Authorization") String authorizationHeader) {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            // delete the entity
            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);

            if(continuousTreeModel == null) {
                logger.log("No content with that session id: "+ sessionId, ILogger.INFO);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            modelRepository.delete(continuousTreeModel);

            storageService.deleteSubdirectory(sessionId);

            logger.log("tree file successfully deleted.", ILogger.INFO);

            logger.log("continuousTreeModelEntity with id " + sessionId + " successfully deleted.",
                       ILogger.INFO);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(path = "/attributes", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Set<String>> attributes(@RequestHeader(value = "Authorization") String authorizationHeader)
        throws IOException, ImportException {
        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);

            Set<String> uniqueAttributes = continuousTreeModel.getAttributes().stream().map(attribute -> {
                    return attribute.getName();
                }).collect(Collectors.toSet());

            return ResponseEntity.ok().body(uniqueAttributes);
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return null;
        }
    }

    @RequestMapping(path = "/hpd-levels", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Set<String>> hpdLevels(@RequestHeader(value = "Authorization") String authorizationHeader)
        throws IOException, ImportException {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);

            Set<AttributeEntity> attributes = continuousTreeModel.getAttributes();
            Set<String> hpdLevels = getHpdLevels(attributes);

            return ResponseEntity.ok().body(hpdLevels);
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return null;
        }
    }

    @RequestMapping(path = "/hpd-level", method = RequestMethod.PUT)
    public ResponseEntity<Object> setHpdLevel(@RequestHeader(value = "Authorization") String authorizationHeader,
                                              @RequestParam(value = "hpd-level", required = true) Integer hpdLevel) {
        try {
            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            Integer min = 0;
            Integer max = 100;

            if (isInInterval(hpdLevel, min, max)) {
                ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);
                continuousTreeModel.setHpdLevel(hpdLevel);
                modelRepository.save(continuousTreeModel);
                logger.log("hpd level parameter successfully set.", ILogger.INFO);

                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                String message = "value is outside of permitted interval [" + min + "," + max + "]";
                logger.log(message, ILogger.ERROR);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
            }

        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(value = { "/coordinates/y", "/coordinates/latitude" }, method = RequestMethod.PUT)
    public ResponseEntity<Object> setyCoordinates(@RequestHeader(value = "Authorization") String authorizationHeader,
                                                  @RequestParam(value = "attribute", required = true) String attribute) {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);
            continuousTreeModel.setyCoordinate(attribute);
            modelRepository.save(continuousTreeModel);

            logger.log("y coordinate successfully set.", ILogger.INFO);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(value = { "/coordinates/x", "/coordinates/longitude" }, method = RequestMethod.PUT)
    public ResponseEntity<Object> setxCoordinates(@RequestHeader(value = "Authorization") String authorizationHeader,
                                                  @RequestParam(value = "attribute", required = true) String attribute) {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);
            continuousTreeModel.setxCoordinate(attribute);
            modelRepository.save(continuousTreeModel);

            logger.log("x coordinate successfully set.", ILogger.INFO);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(path = "/external-annotations", method = RequestMethod.PUT)
    public ResponseEntity<Object> setHasExternalAnnotations(
                                                            @RequestHeader(value = "Authorization") String authorizationHeader,
                                                            @RequestParam(value = "has-external-annotations", required = true) Boolean hasExternalAnnotations) {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);
            continuousTreeModel.setHasExternalAnnotations(hasExternalAnnotations);
            modelRepository.save(continuousTreeModel);

            logger.log("external annotations parameter successfully set.", ILogger.INFO);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(path = "/mrsd", method = RequestMethod.PUT)
    public ResponseEntity<Object> setMrsd(@RequestHeader(value = "Authorization") String authorizationHeader,
                                          @RequestParam(value = "mrsd") String mrsd) {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            if (TimeParser.isParseableDate(mrsd)) {

                ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);
                continuousTreeModel.setMrsd(mrsd);
                modelRepository.save(continuousTreeModel);

                logger.log("Mrsd parameter successfully set.", ILogger.INFO);
                return new ResponseEntity<>(HttpStatus.OK);

            } else {
                String message = "mrsd parameter is in a wrong format. Use " + "yyyy" + TimeParser.separator + "MM"
                    + TimeParser.separator + "dd" + " format.";
                logger.log(message, ILogger.ERROR);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
            }

        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(path = "/timescale-multiplier", method = RequestMethod.PUT)
    public ResponseEntity<Object> setTimescaleMultiplier(
                                                         @RequestHeader(value = "Authorization") String authorizationHeader,
                                                         @RequestParam(value = "timescale-multiplier", required = true) Double timescaleMultiplier) {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            Double min = Double.MIN_NORMAL;
            Double max = Double.MAX_VALUE;

            if (isInInterval(timescaleMultiplier, min, max)) {

                ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);
                continuousTreeModel.setTimescaleMultiplier(timescaleMultiplier);
                modelRepository.save(continuousTreeModel);

                logger.log("timescale multiplier parameter successfully set.", ILogger.INFO);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                String message = "value is outside of permitted interval [" + min + "," + max + "]";
                logger.log(message, ILogger.ERROR);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
            }

        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(path = "/geojson", method = RequestMethod.PUT)
    public ResponseEntity<Object> uploadGeojson(@RequestHeader(value = "Authorization") String authorizationHeader,
                                                @RequestParam(value = "geojsonfile", required = true) MultipartFile file)  {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);

            String sessionId = getSessionId(authorizationHeader);
            String filename = file.getOriginalFilename();

            HttpStatus status = HttpStatus.NO_CONTENT;
            if (storageService.exists(sessionId, file)) {
                storageService.delete(sessionId, filename);
                logger.log("Deleting previously uploaded geojson file: " + filename, ILogger.INFO);
                status = HttpStatus.CREATED;
            } else {
                status = HttpStatus.OK;
            }

            storageService.store(sessionId, file);

            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);
            continuousTreeModel.setGeojsonFilename(
                                                   storageService.loadAsResource(sessionId, filename).getFile().getAbsolutePath());
            modelRepository.save(continuousTreeModel);

            logger.log("geojson file successfully uploaded.", ILogger.INFO);

            return new ResponseEntity<>(status);
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IOException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    //	@RequestMapping(path = "/geojson", method = RequestMethod.DELETE)
    //	public ResponseEntity<Object> deleteGeojson(@RequestHeader(value = "Authorization") String authorizationHeader,
    //			@RequestParam(value = "geojsonfile", required = true) String filename) throws IOException {
    //
    //		try {
    //
    //			logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
    //			String sessionId = getSessionId(authorizationHeader);
    //
    //			ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);
    //			continuousTreeModel.setGeojsonFilename(null);
    //			modelRepository.save(continuousTreeModel);
    //
    //			storageService.delete(filename);
    //			logger.log("geojson file successfully deleted.", ILogger.INFO);
    //
    //			return new ResponseEntity<>(HttpStatus.OK);
    //
    //		} catch (SignatureException e) {
    //			logger.log(Utils.getStackTrace(e), ILogger.ERROR);
    //			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    //		}
    //	}

    @RequestMapping(path = "/output", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<Object> getOutput(@RequestHeader(value = "Authorization") String authorizationHeader) {

        try {


            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);

            // run in background
            taskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            logger.log("Generating output" , ILogger.INFO);

                            String json = doGenerateOutput(continuousTreeModel);
                            // persist in storageDir as data.json
                            storageService.store(sessionId, "data.json", json.getBytes());

                            // update model
                            continuousTreeModel.setOutputFilename(storageService.loadAsResource(sessionId, "data.json").getFile().getAbsolutePath());
                            continuousTreeModel.setStatus(ContinuousTreeModelEntity.Status.OUTPUT_READY);
                            modelRepository.save(continuousTreeModel);

                            logger.log("Output succesfully generated", ILogger.INFO);
                        } catch (IOException e) {
                            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
                        } catch (ImportException e) {
                            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
                        } catch (SpreadException e) {
                            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
                        }
                    }
                });

            continuousTreeModel.setStatus(ContinuousTreeModelEntity.Status.GENERATING_OUTPUT);
            modelRepository.save(continuousTreeModel);

            // return immediately
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", sessionId);
            return new ResponseEntity<>(responseHeaders, HttpStatus.ACCEPTED);
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(path = "/ipfs", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<Object> putIpfs(@RequestHeader(value = "Authorization") String authorizationHeader) {
        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);

            // run in background
            taskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            logger.log("Copying visualisation" , ILogger.INFO);
                            Path source = visualizationService.getVisualisationDirectory();
                            storageService.copy(sessionId, source);
                            logger.log("Copied visualisation from " + source.toString() + " to " + sessionId , ILogger.INFO);

                            logger.log("Publishing to ipfs" , ILogger.INFO);
                            String hash = ipfsService.addDirectory(storageService.getSubdirectoryLocation(sessionId));
                            continuousTreeModel.setIpfsHash(hash);
                            continuousTreeModel.setStatus(ContinuousTreeModelEntity.Status.IPFS_HASH_READY);
                            modelRepository.save(continuousTreeModel);

                            logger.log("Published to Ipfs with hash: " + hash , ILogger.INFO);
                        } catch (IOException e) {
                            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
                        }
                    }
                });

            continuousTreeModel.setStatus(ContinuousTreeModelEntity.Status.PUBLISHING_IPFS);
            modelRepository.save(continuousTreeModel);

            // return immediately
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", sessionId);
            return new ResponseEntity<>(responseHeaders, HttpStatus.ACCEPTED);
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(path = "/ipfs", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getIpfsHash(@RequestHeader(value = "Authorization") String authorizationHeader) {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);

            if(continuousTreeModel.getStatus() == ContinuousTreeModelEntity.Status.IPFS_HASH_READY) {
                return ResponseEntity.ok().header(new HttpHeaders().toString()).body(continuousTreeModel.getIpfsHash().toString());
            } else {
                // client should poll
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.set("Location", "/continuous/status");
                return new ResponseEntity<>(responseHeaders, HttpStatus.SEE_OTHER);
            }

        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @RequestMapping(path = "/status", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getStatus(@RequestHeader(value = "Authorization") String authorizationHeader)
    {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);

            return ResponseEntity.ok().header(new HttpHeaders().toString()).body(continuousTreeModel.getStatus().toString());
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // TODO : return entity in JSON
    @RequestMapping(path = "/model", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getModel(@RequestHeader(value = "Authorization") String authorizationHeader)
    {

        try {

            logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
            String sessionId = getSessionId(authorizationHeader);

            ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);

            // TODO: to JSON
            return ResponseEntity.ok().header(new HttpHeaders().toString()).body(continuousTreeModel.toString());
        } catch (SignatureException e) {
            logger.log(Utils.getStackTrace(e), ILogger.ERROR);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    private String doGenerateOutput (ContinuousTreeModelEntity continuousTreeModel) throws IOException, ImportException, SpreadException {

        TimeLine timeLine = null;
        LinkedList<Attribute> mapAttributes = null;
        LinkedList<Attribute> lineAttributes = null;
        LinkedList<Attribute> pointAttributes = null;
        LinkedList<Attribute> areaAttributes = null;
        LinkedList<Layer> layersList = new LinkedList<Layer>();

        // ---IMPORT---//

        RootedTree rootedTree = Utils.importRootedTree(continuousTreeModel.getTreeFilename());
        TimeParser timeParser = new TimeParser(continuousTreeModel.getMrsd());

        timeLine = timeParser.getTimeLine(rootedTree.getHeight(rootedTree.getRootNode()));

        logger.log("Parsed time line", ILogger.INFO);

        ContinuousTreeParser treeParser = new ContinuousTreeParser(rootedTree, //
                                                                   continuousTreeModel.getxCoordinate(), //
                                                                   continuousTreeModel.getyCoordinate(), //
                                                                   continuousTreeModel.hasExternalAnnotations(), //
                                                                   continuousTreeModel.getHpdLevel().toString(), //
                                                                   timeParser, //
                                                                   continuousTreeModel.getTimescaleMultiplier());

        treeParser.parseTree();

        logger.log("Parsed the tree", ILogger.INFO);

        lineAttributes = treeParser.getLineAttributes();
        pointAttributes = treeParser.getPointAttributes();
        areaAttributes = treeParser.getAreaAttributes();

        logger.log("Parsed tree attributes", ILogger.INFO);

        // ---GEOJSON LAYER---//

        if (continuousTreeModel.getGeojsonFilename() != null) {

            GeoJSONParser geojsonParser = new GeoJSONParser(continuousTreeModel.getGeojsonFilename());
            GeoJsonData geojson = geojsonParser.parseGeoJSON();

            mapAttributes = geojsonParser.getUniqueMapAttributes();

            String geojsonLayerId = Utils.splitString(continuousTreeModel.getGeojsonFilename(), "/");
            Layer geojsonLayer = new Layer(geojsonLayerId, //
                                           "GeoJson layer", //
                                           geojson);

            layersList.add(geojsonLayer);

            logger.log("Parsed map attributes", ILogger.INFO);
        } // END: null check

        // ---DATA LAYER (TREE LINES & POINTS, AREAS)---//

        LinkedList<Line> linesList = treeParser.getLinesList();
        LinkedList<Point> pointsList = treeParser.getPointsList();
        LinkedList<Area> areasList = treeParser.getAreasList();

        String treeLayerId = Utils.splitString(continuousTreeModel.getTreeFilename(), "/");
        Layer treeLayer = new Layer(treeLayerId, //
                                    "Tree layer", //
                                    pointsList, //
                                    linesList, //
                                    areasList);
        layersList.add(treeLayer);

        AxisAttributes axis = new AxisAttributes(continuousTreeModel.getxCoordinate(),
                                                 continuousTreeModel.getyCoordinate());

        SpreadData spreadData = new SpreadData(timeLine, //
                                               axis, //
                                               mapAttributes, //
                                               lineAttributes, //
                                               pointAttributes, //
                                               areaAttributes, //
                                               null, // locations
                                               layersList);

        return new GsonBuilder().create().toJson(spreadData);
    }

    private Set<String> getHpdLevels(Set<AttributeEntity> attributeEntities) {

        Set<String> hpdAttributes = attributeEntities.stream().map(attribute -> {
                return attribute.getName();
            }).filter(attributeName -> attributeName.contains("HPD_modality"))
            .map(hpdString -> hpdString.replaceAll("\\D+", "")).collect(Collectors.toSet());

        return hpdAttributes;
    }

    private String getSessionId(String authorizationHeader) {
        String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
        return TokenUtils.parseJWT(TokenUtils.getBearerToken(authorizationHeader), secret).get(TokenUtils.SESSION_ID)
            .toString();
    }

    private Boolean isInInterval(Double value, Double min, Double max) {
        if (value >= min && value <= max)
            return true;
        return false;
    }

    private Boolean isInInterval(Integer value, Integer min, Integer max) {
        if (value >= min && value <= max)
            return true;
        return false;
    }

}
