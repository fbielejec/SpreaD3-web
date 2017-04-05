package com.spread.controllers;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.spread.domain.ContinuousTreeModelEntity;
import com.spread.exceptions.SpreadException;
import com.spread.loggers.ILogger;
import com.spread.loggers.LoggerFactory;
import com.spread.parsers.ContinuousTreeParser;
import com.spread.parsers.TimeParser;
import com.spread.repositories.ContinuousTreeModelRepository;
import com.spread.services.storage.StorageService;
import com.spread.utils.Utils;

import jebl.evolution.io.ImportException;
import jebl.evolution.trees.RootedTree;

@Controller
@RequestMapping("/continuous")
public class ContinuousTreeController {

	private final ILogger logger;
	private final StorageService storageService;

	@Autowired
	private ContinuousTreeModelRepository repository;

	public ContinuousTreeController(StorageService storageService) {
		this.logger = new LoggerFactory().getLogger(LoggerFactory.DEFAULT);
		this.storageService = storageService;
	}

	@RequestMapping(path = "/tree", method = RequestMethod.POST)
	public ResponseEntity<Void> uploadTree(@RequestParam(value = "treefile", required = true) MultipartFile file)
			throws IOException {

		// store the file
		storageService.store(file);

		ContinuousTreeModelEntity continuousTreeModel = new ContinuousTreeModelEntity();
		continuousTreeModel
				.setTreeFilename(storageService.loadAsResource(file.getOriginalFilename()).getFile().getAbsolutePath());
		repository.save(continuousTreeModel);

		logger.log("tree file successfully persisted.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/tree", method = RequestMethod.DELETE)
	public ResponseEntity<Void> deleteTree(@RequestParam(value = "treefile", required = true) String filename) {

		// delete the file
		storageService.delete(filename);

		// delete the entity
		ContinuousTreeModelEntity continuousTreeModel = repository.findAll().get(0);
		repository.delete(continuousTreeModel);

		logger.log("tree file successfully deleted.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/attributes", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Set<String>> attributes() throws IOException, ImportException {

		ContinuousTreeModelEntity continuousTreeModel = repository.findAll().get(0);

		RootedTree tree = Utils.importRootedTree(continuousTreeModel.getTreeFilename());
		Set<String> uniqueAttributes = tree.getNodes().stream().filter(node -> !tree.isRoot(node))
				.flatMap(node -> node.getAttributeNames().stream()).collect(Collectors.toSet());

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + continuousTreeModel.getTreeFilename() + "\"")
				.body(uniqueAttributes);
	}

	@RequestMapping(value = { "/coordinates/y", "/coordinates/latitude" }, method = RequestMethod.POST)
	public ResponseEntity<Void> setyCoordinates(@RequestParam(value = "attribute", required = true) String attribute) {

		ContinuousTreeModelEntity continuousTreeModel = repository.findAll().get(0);
		continuousTreeModel.setyCoordinate(attribute);
		repository.save(continuousTreeModel);

		logger.log("y coordinate successfully set.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = { "/coordinates/x", "/coordinates/longitude" }, method = RequestMethod.POST)
	public ResponseEntity<Void> setxCoordinates(@RequestParam(value = "attribute", required = true) String attribute) {

		ContinuousTreeModelEntity continuousTreeModel = repository.findAll().get(0);
		continuousTreeModel.setxCoordinate(attribute);
		repository.save(continuousTreeModel);

		logger.log("x coordinate successfully set.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/external-annotations", method = RequestMethod.POST)
	public ResponseEntity<Void> setHasExternalAnnotations(
			@RequestParam(value = "has-external-annotations", required = true) Boolean hasExternalAnnotations) {

		ContinuousTreeModelEntity continuousTreeModel = repository.findAll().get(0);
		continuousTreeModel.setHasExternalAnnotations(hasExternalAnnotations);
		repository.save(continuousTreeModel);

		logger.log("external annotations parameter successfully set.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/hpd-level", method = RequestMethod.POST)
	public ResponseEntity<Object> setHpdLevel(@RequestParam(value = "hpd-level", required = true) Double hpdLevel) {
		try {

			checkInterval(hpdLevel, 0.0, 1.0);

			ContinuousTreeModelEntity continuousTreeModel = repository.findAll().get(0);
			continuousTreeModel.setHpdLevel(hpdLevel);
			repository.save(continuousTreeModel);

			logger.log("hpd level parameter successfully set.", ILogger.INFO);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (SpreadException e) {
			logger.log(e.getMessage(), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
		}
	}

	@RequestMapping(path = "/timescale-multiplier", method = RequestMethod.POST)
	public ResponseEntity<Object> setTimescaleMultiplier(
			@RequestParam(value = "timescale-multiplier", required = true) Double timescaleMultiplier) {
		
		try {
			checkInterval(timescaleMultiplier, Double.MIN_NORMAL, Double.MAX_VALUE);

			ContinuousTreeModelEntity continuousTreeModel = repository.findAll().get(0);
			continuousTreeModel.setTimescaleMultiplier(timescaleMultiplier);
			repository.save(continuousTreeModel);

			logger.log("timescale multiplier parameter successfully set.", ILogger.INFO);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (SpreadException e) {
			logger.log(e.getMessage(), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
		}
	}

	@RequestMapping(path = "/geojson", method = RequestMethod.POST)
	public ResponseEntity<Object> uploadGeojson(
			@RequestParam(value = "geojsonfile", required = true) MultipartFile file) throws IOException {

		storageService.store(file);

		ContinuousTreeModelEntity continuousTreeModel = repository.findAll().get(0);
		continuousTreeModel.setGeojsonFilename(
				storageService.loadAsResource(file.getOriginalFilename()).getFile().getAbsolutePath());
		repository.save(continuousTreeModel);

		logger.log("geojson file successfully uploaded.", ILogger.INFO);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/geojson", method = RequestMethod.DELETE)
	public ResponseEntity<Object> deleteGeojson(@RequestParam(value = "geojsonfile", required = true) String filename)
			throws IOException {

		storageService.delete(filename);

		ContinuousTreeModelEntity continuousTreeModel = repository.findAll().get(0);
		continuousTreeModel.setGeojsonFilename(null);
		repository.save(continuousTreeModel);

		logger.log("geojson file successfully deleted.", ILogger.INFO);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	// TODO
	@RequestMapping(path = "/output", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getOutput() {

		try {
			
			ContinuousTreeModelEntity continuousTreeModel = repository.findAll().get(0);

			RootedTree rootedTree = Utils.importRootedTree(continuousTreeModel.getTreeFilename());
			TimeParser timeParser = new TimeParser(continuousTreeModel.getMrsd());

			logger.log("Parsed time line", ILogger.INFO);
			
			ContinuousTreeParser treeParser = new ContinuousTreeParser(rootedTree, //
					continuousTreeModel.getxCoordinate(), //
					continuousTreeModel.getyCoordinate(), //
					continuousTreeModel.hasExternalAnnotations(), //
					continuousTreeModel.getHpdLevel().toString(), //
					timeParser, //
					continuousTreeModel.getTimescaleMultiplier());

			treeParser.parseTree();

			return null;

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

		ContinuousTreeModelEntity continuousTreeModel = repository.findAll().get(0);

		return ResponseEntity.ok().header(new HttpHeaders().toString()).body(continuousTreeModel);
	}

	private void checkInterval(Double value, Double min, Double max) throws SpreadException {
		if (value >= min && value <= max) {
			return;
		} else {
			throw new SpreadException("value is outside of permitted interval [" + min + "," + max + "]");
		}
	}

}
