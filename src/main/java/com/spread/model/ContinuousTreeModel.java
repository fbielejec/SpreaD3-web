package com.spread.model;

public class ContinuousTreeModel {

	// ---REQUIRED---//

	public String tree  = null;
	public String getTree() {
		return tree;
	}
	public void setTree(String tree) {
		this.tree = tree;
	}

	// continuous coordinate attribute names
	public String xCoordinate = null; // long
	public String yCoordinate = null; // lat

	public String hpd = "";
	
	// ---OPTIONAL---//

	// most recent sampling date yyy/mm/dd
	public String mrsd = "0/0/0";

	// multiplier for the branch lengths. Defaults to 1 unit = 1 year
	public double timescaleMultiplier = 1.0;

	// path to json output file
	public String outputFilename = "output.json";

	public String geojsonFilename = null;

	public boolean externalAnnotations = false;
	
}
