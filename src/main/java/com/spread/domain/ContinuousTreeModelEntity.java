package com.spread.domain;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "continuous_tree_model")
public class ContinuousTreeModelEntity {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "session_id", nullable = false)
	private String sessionId;
	
	@Column(name = "tree_filename", nullable = false)
	private String treeFilename;

	@Column(name = "x_coordinate", nullable = true)
	private String xCoordinate; // long

	@Column(name = "y_coordinate", nullable = true)
	private String yCoordinate; // lat

	@Column(name = "hpd_level", nullable = true)
	private Double hpdLevel;

	@Column(name = "mrsd", nullable = true)
	private String mrsd;

	@Column(name = "timescale_multiplier", nullable = true)
	private double timescaleMultiplier;

	@Column(name = "output_filename", nullable = true)
	private String outputFilename;

	@Column(name = "geojson_filename", nullable = true)
	private String geojsonFilename;

	@Column(name = "has_external_annotations", nullable = true)
	private boolean hasExternalAnnotations;

	@OneToMany(mappedBy = "tree", cascade = CascadeType.ALL)
	private Set<AttributeEntity> attributes;

	public ContinuousTreeModelEntity() {
	}
	
	public ContinuousTreeModelEntity(String absolutePath, String sessionId) {
		this.treeFilename = absolutePath;
		this.sessionId = sessionId;
	}

	public Integer getId() {
		return this.id;
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getTreeFilename() {
		return treeFilename;
	}

	public void setTreeFilename(String treeFilename) {
		this.treeFilename = treeFilename;
	}
	
	public Set<AttributeEntity> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<AttributeEntity> attributes) {
		this.attributes = attributes;
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

}
