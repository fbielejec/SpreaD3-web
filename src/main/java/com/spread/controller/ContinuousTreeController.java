package com.spread.controller;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.spread.controller.loggers.ILogger;
import com.spread.controller.loggers.LoggerFactory;
import com.spread.model.ContinuousTreeModelDTO;
import com.spread.model.storage.StorageService;
import com.spread.utils.Utils;

import jebl.evolution.io.ImportException;
import jebl.evolution.trees.RootedTree;

@RestController
@RequestMapping("continuous")
public class ContinuousTreeController {

    private final ILogger logger;
	private final StorageService storageService;
	private final ContinuousTreeModelDTO dto;

	@Autowired
	public ContinuousTreeController(StorageService storageService) {
		this.logger = new LoggerFactory().getLogger(LoggerFactory.DEFAULT);
		this.storageService = storageService;
		this.dto = new ContinuousTreeModelDTO();
	}

	@RequestMapping(path = "/geojson", method = RequestMethod.POST)
	public ResponseEntity<Object> uploadGeojson(@RequestParam(value = "geojsonfile", required = true) MultipartFile file) throws IOException {
		storageService.store(file);
		dto.setGeojsonFilename(storageService.loadAsResource(file.getOriginalFilename()).getFile().getAbsolutePath());
		logger.log("geojson file successfully uploaded.", ILogger.INFO);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(path = "/geojson", method = RequestMethod.DELETE)
	public ResponseEntity<Object> deleteGeojson(@RequestParam(value = "geojsonfile", required = true) MultipartFile file) throws IOException {
		storageService.store(file);
		dto.setGeojsonFilename(storageService.loadAsResource(file.getOriginalFilename()).getFile().getAbsolutePath());
		logger.log("geojson file successfully deleted.", ILogger.INFO);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(path = "/tree", method = RequestMethod.POST)
	public ResponseEntity<Void> uploadTree(@RequestParam(value = "treefile", required = true) MultipartFile file)
			throws IOException {
		storageService.store(file);
		dto.setTreeFilename(storageService.loadAsResource(file.getOriginalFilename()).getFile().getAbsolutePath());
		logger.log("tree file successfully uploaded.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/tree", method = RequestMethod.DELETE)
	public ResponseEntity<Void> deleteTree(@RequestParam(value = "treefile", required = true) String filename) {
		storageService.delete(filename);
		logger.log("tree file successfully deleted.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/attributes", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Set<String>> attributes() throws IOException, ImportException {

		RootedTree tree = Utils.importRootedTree(dto.getTreeFilename());
		Set<String> uniqueAttributes = tree.getNodes().stream().filter(node -> !tree.isRoot(node))
				.flatMap(node -> node.getAttributeNames().stream()).collect(Collectors.toSet());
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.getTreeFilename() + "\"")
				.body(uniqueAttributes);
	}

	@RequestMapping(value = { "/coordinates/y", "/coordinates/latitude" }, method = RequestMethod.POST)
	public ResponseEntity<Void> setyCoordinates(@RequestParam(value = "attribute", required = true) String attribute) {
		dto.setyCoordinate(attribute);
		logger.log("y coordinate successfully set.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = { "/coordinates/x", "/coordinates/longitude" }, method = RequestMethod.POST)
	public ResponseEntity<Void> setxCoordinates(@RequestParam(value = "attribute", required = true) String attribute) {
		dto.setxCoordinate(attribute); 
		logger.log("x coordinate successfully set.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/external-annotations", method = RequestMethod.POST)
	public ResponseEntity<Void> setHasExternalAnnotations(@RequestParam(value = "has-external-annotations", required = true) Boolean hasExternalAnnotations) {
		dto.setHasExternalAnnotations(hasExternalAnnotations);   
		logger.log("external annotations parameter successfully set.", ILogger.INFO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/hpd-level", method = RequestMethod.POST)
	public ResponseEntity<Object> setHpdLevel(@RequestParam(value = "hpd-level", required = true) Double hpdLevel) {
		try {
			checkInterval(hpdLevel, 0.0, 1.0);
			dto.setHpdLevel(hpdLevel);
			logger.log("hpd level parameter successfully set.", ILogger.INFO);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (ControllerException e) {
			logger.log(e.getMessage(), ILogger.ERROR);
			return ResponseEntity
			            .status(HttpStatus.UNPROCESSABLE_ENTITY)
			            .body(e.getMessage());
		}
	}

	@RequestMapping(path = "/timescale-multiplier", method = RequestMethod.POST)
	public ResponseEntity<Object> setTimescaleMultiplier(@RequestParam(value = "timescale-multiplier", required = true) Double timescaleMultiplier) {
		try {
			checkInterval(timescaleMultiplier, Double.MIN_NORMAL, Double.MAX_VALUE);
			dto.setTimescaleMultiplier(timescaleMultiplier);
			logger.log("timescale multiplier parameter successfully set.", ILogger.INFO);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (ControllerException e) {
			logger.log(e.getMessage(), ILogger.ERROR);
			return ResponseEntity
		            .status(HttpStatus.UNPROCESSABLE_ENTITY)
		            .body(e.getMessage());
		}
	}
	
	@RequestMapping(path = "/model", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<ContinuousTreeModelDTO> getModel() throws IOException, ImportException {
		return ResponseEntity.ok()
				.header(new HttpHeaders().toString())
				.body(dto);
	}
	
	private void checkInterval(Double value, Double min, Double max) throws ControllerException {
		if(value >= min && value <= max) {
			return;
		} else { 
			throw new ControllerException("value is outside of permitted interval [" + min + "," + max + "]");
		}
	}
	
}
