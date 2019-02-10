package com.spread.domain;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "continuous_tree_models")
public class ContinuousTreeModelEntity {

	@Id
	@Column(name = "id", nullable = false)
	private String sessionId;

	// use primary key of Session as the id	
	// delete corresponding session
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "id")
	@MapsId
	private SessionEntity session;

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
	private Boolean hasExternalAnnotations;

	@OneToMany(mappedBy = "tree", cascade = CascadeType.ALL)
	private Set<AttributeEntity> attributes;

	public ContinuousTreeModelEntity() {
	}

	public ContinuousTreeModelEntity(String absolutePath, SessionEntity session) {
		this.treeFilename = absolutePath;
		this.session = session;
	}

	public String getId() {
		return this.session.getId();
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

	public Double getTimescaleMultiplier() {
		return timescaleMultiplier;
	}

	public void setTimescaleMultiplier(Double timescaleMultiplier) {
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

	public void setHasExternalAnnotations(Boolean hasExternalAnnotations) {
		this.hasExternalAnnotations = hasExternalAnnotations;
	}

}
