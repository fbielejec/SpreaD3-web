package com.spread.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "continuous_tree_model")
@NamedQueries(value = {
		@NamedQuery(name = ContinuousTreeModelEntity.FIND_BY_ID, query = "from continuous_tree_model e where e.id=:id") })
public class ContinuousTreeModelEntity implements LongID {

	public static final String FIND_BY_ID = "ContinuousTreeModelEntity.byId";

	private Long id;

	private String treeFilename = null;
	private String xCoordinate = null; // long
	private String yCoordinate = null; // lat
	private Double hpdLevel;
	private String mrsd = "0/0/0";
	private double timescaleMultiplier = 1.0;
	private String outputFilename = "output.json";
	private String geojsonFilename = null;
	private boolean hasExternalAnnotations = false;

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

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

	public ContinuousTreeModelDTO toDto() {
		return new ContinuousTreeModelDTO(treeFilename, xCoordinate, yCoordinate, hpdLevel, mrsd, timescaleMultiplier,
				outputFilename, geojsonFilename, hasExternalAnnotations);
	}

}
