package com.spread.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "continuous_tree_model")
public class ContinuousTreeModelEntity {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "tree_filename", nullable = false)
	private String treeFilename;

	@Column
	private String xCoordinate; // long

	@Column
	private String yCoordinate; // lat

	@Column
	private Double hpdLevel;

	@Column
	private String mrsd;

	@Column
	private double timescaleMultiplier; // 1.0

	@Column
	private String outputFilename;// = "output.json";

	@Column(name = "geojson_filename", nullable = true)
	private String geojsonFilename;

	@Column
	private boolean hasExternalAnnotations; // false;

	public String getxCoordinate() {
		return xCoordinate;
	}

	public void setxCoordinate(String xCoordinate) {
		this.xCoordinate = xCoordinate;
	}

	public String getyCoordinate() {
		return yCoordinate;
	}

	public void setyCoordinate(String yCoordinate) {
		this.yCoordinate = yCoordinate;
	}

	public Double getHpdLevel() {
		return hpdLevel;
	}

	public void setHpdLevel(Double hpdLevel) {
		this.hpdLevel = hpdLevel;
	}

	public String getMrsd() {
		return mrsd;
	}

	public void setMrsd(String mrsd) {
		this.mrsd = mrsd;
	}

	public double getTimescaleMultiplier() {
		return timescaleMultiplier;
	}

	public void setTimescaleMultiplier(double timescaleMultiplier) {
		this.timescaleMultiplier = timescaleMultiplier;
	}

	public String getOutputFilename() {
		return outputFilename;
	}

	public void setOutputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
	}

	public String getGeojsonFilename() {
		return geojsonFilename;
	}

	public void setGeojsonFilename(String geojsonFilename) {
		this.geojsonFilename = geojsonFilename;
	}

	public boolean hasExternalAnnotations() {
		return hasExternalAnnotations;
	}

	public void setHasExternalAnnotations(boolean hasExternalAnnotations) {
		this.hasExternalAnnotations = hasExternalAnnotations;
	}

	public String getTreeFilename() {
		return treeFilename;
	}

	public void setTreeFilename(String treeFilename) {
		this.treeFilename = treeFilename;
	}

	// public ContinuousTreeModelDTO toDto() {
	// return new ContinuousTreeModelDTO(treeFilename, xCoordinate, yCoordinate,
	// hpdLevel, mrsd, timescaleMultiplier,
	// outputFilename, geojsonFilename, hasExternalAnnotations);
	// }

}
