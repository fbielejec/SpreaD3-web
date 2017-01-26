package com.spread.utils;

import java.io.FileReader;
import java.io.IOException;

import jebl.evolution.io.ImportException;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.trees.RootedTree;

public class Utils {

	public static RootedTree importRootedTree(String tree) throws IOException, ImportException {
		TreeImporter importer = new NexusImporter(new FileReader(tree));
		RootedTree rootedTree = (RootedTree) importer.importNextTree();
		return rootedTree;
	} 
	
}
