package com.spread.controllers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.spread.exceptions.SpreadException;
import com.spread.loggers.ILogger;
import com.spread.loggers.LoggerFactory;
import com.spread.parsers.ContinuousTreeParser;
import com.spread.parsers.GeoJSONParser;
import com.spread.parsers.TimeParser;
import com.spread.repositories.AttributeRepository;
import com.spread.repositories.ContinuousTreeModelRepository;
import com.spread.services.storage.StorageService;
import com.spread.utils.Utils;

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
	private AttributeRepository attributeRepository;

	public ContinuousTreeController(StorageService storageService) {
		this.logger = new LoggerFactory().getLogger(LoggerFactory.DEFAULT);
		this.storageService = storageService;
	}

	@RequestMapping(path = "/tree", method = RequestMethod.POST)
	public ResponseEntity<Object> uploadTree(@RequestParam(value = "treefile", required = true) MultipartFile file) {
		try {

			String filename = file.getOriginalFilename();

			if (storageService.exists(file)) {
				storageService.delete(filename);
				logger.log("Deleting previously uploaded tree file: " + filename, ILogger.INFO);
			}

			storageService.store(file);

			logger.log("tree file " + filename + " successfully persisted.", ILogger.INFO);

			ContinuousTreeModelEntity continuousTreeModel = new ContinuousTreeModelEntity(
					storageService.loadAsResource(filename).getFile().getAbsolutePath());

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
		} catch (Exception e) {
			logger.log(Utils.getStackTrace(e), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@RequestMapping(path = "/tree", method = RequestMethod.DELETE)
	public ResponseEntity<Void> deleteTree(@RequestParam(value = "treefile", required = true) String filename) {

		// delete the file
		storageService.delete(filename);

		// delete the entity
		ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);
		modelRepository.delete(continuousTreeModel);

		logger.log("tree file successfully deleted.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/attributes", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Set<String>> attributes() throws IOException, ImportException {

		List<AttributeEntity> attributeEntities = attributeRepository.findAll();

		Set<String> uniqueAttributes = attributeEntities.stream().map(attribute -> {
			return attribute.getName();
		}).collect(Collectors.toSet());

		return ResponseEntity.ok().body(uniqueAttributes);
	}

	@RequestMapping(value = { "/coordinates/y", "/coordinates/latitude" }, method = RequestMethod.POST)
	public ResponseEntity<Void> setyCoordinates(@RequestParam(value = "attribute", required = true) String attribute) {

		ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);
		continuousTreeModel.setyCoordinate(attribute);
		modelRepository.save(continuousTreeModel);

		logger.log("y coordinate successfully set.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = { "/coordinates/x", "/coordinates/longitude" }, method = RequestMethod.POST)
	public ResponseEntity<Void> setxCoordinates(@RequestParam(value = "attribute", required = true) String attribute) {

		ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);
		continuousTreeModel.setxCoordinate(attribute);
		modelRepository.save(continuousTreeModel);

		logger.log("x coordinate successfully set.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/external-annotations", method = RequestMethod.POST)
	public ResponseEntity<Void> setHasExternalAnnotations(
			@RequestParam(value = "has-external-annotations", required = true) Boolean hasExternalAnnotations) {

		ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);
		continuousTreeModel.setHasExternalAnnotations(hasExternalAnnotations);
		modelRepository.save(continuousTreeModel);

		logger.log("external annotations parameter successfully set.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private Set<String> getHpdLevels(RootedTree tree) {

		List<AttributeEntity> attributeEntities = attributeRepository.findAll();

		Set<String> hpdAttributes = attributeEntities.stream().map(attribute -> {
			return attribute.getName();
		}).filter(attributeName -> attributeName.contains("HPD_modality"))
				.map(hpdString -> hpdString.replaceAll("\\D+", "")).collect(Collectors.toSet());

		return hpdAttributes;
	}

	@RequestMapping(path = "/hpd-levels", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Set<String>> hpdLevels() throws IOException, ImportException {

		ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);

		RootedTree tree = Utils.importRootedTree(continuousTreeModel.getTreeFilename());
		Set<String> hpdLevels = getHpdLevels(tree);

		return ResponseEntity.ok().body(hpdLevels);
	}

	@RequestMapping(path = "/hpd-level", method = RequestMethod.POST)
	public ResponseEntity<Object> setHpdLevel(@RequestParam(value = "hpd-level", required = true) Double hpdLevel) {

		Double min = 0.0;
		Double max = 1.0;

		if (isInInterval(hpdLevel, min, max)) {
			ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);
			continuousTreeModel.setHpdLevel(hpdLevel);
			modelRepository.save(continuousTreeModel);
			logger.log("hpd level parameter successfully set.", ILogger.INFO);

			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			String message = "value is outside of permitted interval [" + min + "," + max + "]";
			logger.log(message, ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(message);
		}
	}

	@RequestMapping(path = "/mrsd", method = RequestMethod.POST)
	public ResponseEntity<Object> setMrsd(@RequestParam(value = "mrsd") String mrsd) {

		ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);
		continuousTreeModel.setMrsd(mrsd);
		modelRepository.save(continuousTreeModel);

		logger.log("Mrsd parameter successfully set.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/timescale-multiplier", method = RequestMethod.POST)
	public ResponseEntity<Object> setTimescaleMultiplier(
			@RequestParam(value = "timescale-multiplier", required = true) Double timescaleMultiplier) {

		Double min = Double.MIN_NORMAL;
		Double max = Double.MAX_VALUE;

		if (isInInterval(timescaleMultiplier, min, max)) {

			ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);
			continuousTreeModel.setTimescaleMultiplier(timescaleMultiplier);
			modelRepository.save(continuousTreeModel);

			logger.log("timescale multiplier parameter successfully set.", ILogger.INFO);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			String message = "value is outside of permitted interval [" + min + "," + max + "]";
			logger.log(message, ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(message);
		}

	}

	@RequestMapping(path = "/geojson", method = RequestMethod.POST)
	public ResponseEntity<Object> uploadGeojson(
			@RequestParam(value = "geojsonfile", required = true) MultipartFile file) throws IOException {

		String filename = file.getOriginalFilename();

		if (storageService.exists(file)) {
			storageService.delete(filename);
			logger.log("Deleting previously uploaded geojson file: " + filename, ILogger.INFO);
		}

		storageService.store(file);

		ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);
		continuousTreeModel.setGeojsonFilename(
				storageService.loadAsResource(file.getOriginalFilename()).getFile().getAbsolutePath());
		modelRepository.save(continuousTreeModel);

		logger.log("geojson file successfully uploaded.", ILogger.INFO);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/geojson", method = RequestMethod.DELETE)
	public ResponseEntity<Object> deleteGeojson(@RequestParam(value = "geojsonfile", required = true) String filename)
			throws IOException {

		storageService.delete(filename);

		ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);
		continuousTreeModel.setGeojsonFilename(null);
		modelRepository.save(continuousTreeModel);

		logger.log("geojson file successfully deleted.", ILogger.INFO);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/output", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getOutput() {

		try {

			ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);

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
		} catch (IOException e) {
			logger.log(e.getMessage(), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		} catch (ImportException e) {
			logger.log(e.getMessage(), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		} catch (SpreadException e) {
			logger.log(e.getMessage(), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

	}

	@RequestMapping(path = "/model", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<ContinuousTreeModelEntity> getModel() throws IOException, ImportException {

		ContinuousTreeModelEntity continuousTreeModel = modelRepository.findAll().get(0);

		return ResponseEntity.ok().header(new HttpHeaders().toString()).body(continuousTreeModel);
	}

	private Boolean isInInterval(Double value, Double min, Double max) {
		if (value >= min && value <= max)
			return true;
		return false;
	}

	// TODO: boolean
	private void checkIsDate(String date) {
		return;
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
