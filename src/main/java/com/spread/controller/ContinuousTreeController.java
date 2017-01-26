package com.spread.controller;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;
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

import com.spread.model.ContinuousTreeModel;
import com.spread.model.storage.StorageService;
import com.spread.utils.Utils;

import jebl.evolution.io.ImportException;
import jebl.evolution.trees.RootedTree;

@RestController
@RequestMapping("continuous")
public class ContinuousTreeController {

	@SuppressWarnings("unused")
	private final Logger logger = Logger.getLogger(ContinuousTreeController.class.getName());

	private final StorageService storageService;
	private ContinuousTreeModel model;

	@Autowired
	public ContinuousTreeController(StorageService storageService) {
		this.storageService = storageService;
		this.model = new ContinuousTreeModel();
	}

	@RequestMapping(path = "/model", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<ContinuousTreeModel> model() throws IOException, ImportException {
		return ResponseEntity.ok()
				.header(new HttpHeaders().toString())
				.body(model);
	}
	
	
	@RequestMapping(path = "/tree", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<Void> tree(@RequestParam(value = "treefile", required = true) MultipartFile file)
			throws IOException {
		storageService.store(file);
		model.setTree(storageService.loadAsResource(file.getOriginalFilename()).getFile().getAbsolutePath());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/tree", method = RequestMethod.DELETE)
	public ResponseEntity<Void> deleteTree(@RequestParam(value = "treefile", required = true) String filename) {
		storageService.delete(filename);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/attributes", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Set<String>> attributes() throws IOException, ImportException {

		RootedTree tree = Utils.importRootedTree(model.getTree());
		Set<String> uniqueAttributes = tree.getNodes().stream().filter(node -> !tree.isRoot(node))
				.flatMap(node -> node.getAttributeNames().stream()).collect(Collectors.toSet());

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + model.getTree() + "\"")
				.body(uniqueAttributes);
	}

	@RequestMapping(value = { "/coordinates/y", "/coordinates/latitude" }, method = RequestMethod.POST)
	public ResponseEntity<Void> coordinatesY(@RequestParam(value = "attribute", required = true) String attribute) {
		model.yCoordinate = attribute;
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = { "/coordinates/x", "/coordinates/longitude" }, method = RequestMethod.POST)
	public ResponseEntity<Void> coordinatesX(@RequestParam(value = "attribute", required = true) String attribute) {
		model.xCoordinate = attribute;
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/external_annotations", method = RequestMethod.POST)
	public void externalAnnotations(HttpServletRequest request, HttpServletResponse response) {

	}

	@RequestMapping(path = "/hpd_level", method = RequestMethod.POST)
	public void hpdLevel(HttpServletRequest request, HttpServletResponse response) {

	}

	@RequestMapping(path = "/timescale_multiplier", method = RequestMethod.POST)
	public void timescaleMultiplier(HttpServletRequest request, HttpServletResponse response) {

	}

	@RequestMapping(path = "/geojson", method = RequestMethod.POST)
	public void geojson(HttpServletRequest request, HttpServletResponse response) {

	}

}
