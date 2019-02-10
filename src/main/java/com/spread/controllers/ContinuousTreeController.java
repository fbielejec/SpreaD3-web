package com.spread.controllers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.spread.services.storage.StorageService;
import com.spread.utils.TokenUtils;
import com.spread.utils.Utils;

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
	private ContinuousTreeModelRepository modelRepository;

	@Autowired
	private KeyRepository keyRepository;

	public ContinuousTreeController(StorageService storageService) {
		this.logger = new LoggerFactory().getLogger(LoggerFactory.DEFAULT);
		this.storageService = storageService;
	}

	@RequestMapping(path = "/tree", method = RequestMethod.POST)
	public ResponseEntity<Object> uploadTree(@RequestHeader(value = "Authorization") String authorizationHeader,
			@RequestParam(value = "treefile", required = true) MultipartFile file) {

		try {

			logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
			String sessionId = getSessionId(authorizationHeader);

			String filename = file.getOriginalFilename();
			if (storageService.exists(file)) {
				storageService.delete(filename);
				logger.log("Deleting previously uploaded tree file: " + filename, ILogger.INFO);
			}

			storageService.store(file);
			logger.log("tree file " + filename + " successfully persisted.", ILogger.INFO);

			ContinuousTreeModelEntity continuousTreeModel = new ContinuousTreeModelEntity(
					storageService.loadAsResource(filename).getFile().getAbsolutePath(), 
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

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (IOException | ImportException e) {
			logger.log(Utils.getStackTrace(e), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		} catch (SignatureException e) {
			logger.log(Utils.getStackTrace(e), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}

	@RequestMapping(path = "/tree", method = RequestMethod.DELETE)
	public ResponseEntity<Object> deleteTree(@RequestHeader(value = "Authorization") String authorizationHeader,
			@RequestParam(value = "treefile", required = true) String filename) {

		try {

			logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
			String sessionId = getSessionId(authorizationHeader);

			// delete the entity
			ContinuousTreeModelEntity continuousTreeModel = modelRepository.findByTreeFilenameAndSessionId(filename,
					sessionId);
			modelRepository.delete(continuousTreeModel);

			// delete the file
			storageService.delete(filename);

			logger.log("tree file successfully deleted.", ILogger.INFO);
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

	@RequestMapping(path = "/hpd-level", method = RequestMethod.POST)
	public ResponseEntity<Object> setHpdLevel(@RequestHeader(value = "Authorization") String authorizationHeader,
			@RequestParam(value = "hpd-level", required = true) Double hpdLevel) {

		try {

			logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
			String sessionId = getSessionId(authorizationHeader);

			Double min = 0.0;
			Double max = 1.0;

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

	@RequestMapping(value = { "/coordinates/y", "/coordinates/latitude" }, method = RequestMethod.POST)
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

	@RequestMapping(value = { "/coordinates/x", "/coordinates/longitude" }, method = RequestMethod.POST)
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

	@RequestMapping(path = "/external-annotations", method = RequestMethod.POST)
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

	@RequestMapping(path = "/mrsd", method = RequestMethod.POST)
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

	@RequestMapping(path = "/timescale-multiplier", method = RequestMethod.POST)
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

	@RequestMapping(path = "/geojson", method = RequestMethod.POST)
	public ResponseEntity<Object> uploadGeojson(@RequestHeader(value = "Authorization") String authorizationHeader,
			@RequestParam(value = "geojsonfile", required = true) MultipartFile file) throws IOException {

		try {

			logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
			String sessionId = getSessionId(authorizationHeader);

			String filename = file.getOriginalFilename();

			if (storageService.exists(file)) {
				storageService.delete(filename);
				logger.log("Deleting previously uploaded geojson file: " + filename, ILogger.INFO);
			}

			storageService.store(file);

			ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);
			continuousTreeModel.setGeojsonFilename(
					storageService.loadAsResource(file.getOriginalFilename()).getFile().getAbsolutePath());
			modelRepository.save(continuousTreeModel);

			logger.log("geojson file successfully uploaded.", ILogger.INFO);

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (SignatureException e) {
			logger.log(Utils.getStackTrace(e), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}

	@RequestMapping(path = "/geojson", method = RequestMethod.DELETE)
	public ResponseEntity<Object> deleteGeojson(@RequestHeader(value = "Authorization") String authorizationHeader,
			@RequestParam(value = "geojsonfile", required = true) String filename) throws IOException {

		try {

			logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
			String sessionId = getSessionId(authorizationHeader);

			ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);
			continuousTreeModel.setGeojsonFilename(null);
			modelRepository.save(continuousTreeModel);

			storageService.delete(filename);
			logger.log("geojson file successfully deleted.", ILogger.INFO);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (SignatureException e) {
			logger.log(Utils.getStackTrace(e), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}

	@RequestMapping(path = "/output", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getOutput(@RequestHeader(value = "Authorization") String authorizationHeader) {

		try {

			logger.log("Received authorization header: " + authorizationHeader, ILogger.INFO);
			String sessionId = getSessionId(authorizationHeader);

			ContinuousTreeModelEntity continuousTreeModel = modelRepository.findBySessionId(sessionId);

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

			String json = new GsonBuilder().create().toJson(spreadData);

			// TODO: persis in storageDir as treefilename.json
			// String outputFileName = "output.json";
			// FileWriter fw = new FileWriter(new
			// File(storageService.getRootLocation() + "/" + outputFileName));
			// fw.write(json);
			// fw.close();
			// FileInputStream input = new FileInputStream(json);
			// MultipartFile file = new MockMultipartFile(outputFileName,
			// outputFileName, "text/plain",
			// IOUtils.toByteArray(input));
			// storageService.store(file);
			//
			// continuousTreeModel.setOutputFilename(
			// storageService.loadAsResource(file.getOriginalFilename()).getFile().getAbsolutePath());
			// modelRepository.save(continuousTreeModel);

			return ResponseEntity.ok().header(new HttpHeaders().toString()).body(json);
		} catch (IOException | ImportException | SpreadException e) {
			logger.log(e.getMessage(), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		} catch (SignatureException e) {
			logger.log(Utils.getStackTrace(e), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}

	@RequestMapping(path = "/model", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getModel(@RequestHeader(value = "Authorization") String authorizationHeader)
			throws IOException, ImportException {

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

	// private MultipartFile getMultipartFile(String json, String
	// outputFileName)
	// throws IOException, FileNotFoundException {
	// File file = new File(outputFileName);
	// FileWriter fw = new FileWriter(file);
	// fw.write(json);
	// fw.close();
	// FileInputStream input = new FileInputStream(json);
	// MultipartFile multipartFile = new MockMultipartFile(outputFileName,
	// outputFileName, "text/plain",
	// IOUtils.toByteArray(input));
	// return multipartFile;
	// }

}
