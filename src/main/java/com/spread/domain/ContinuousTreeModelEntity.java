package com.spread.domain;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "continuous_tree_models")
public class ContinuousTreeModelEntity implements IModel {

    @Id
    @Column(name="sessionId", nullable = false)
    String sessionId;

    @Column(name = "tree_filename", nullable = false)
    private String treeFilename;

    @Column(name = "x_coordinate", nullable = true)
    private String xCoordinate; // long

    @Column(name = "y_coordinate", nullable = true)
    private String yCoordinate; // lat

    @Column(name = "hpd_level", nullable = true)
    private String hpdLevel;

    @Column(name = "mrsd", nullable = true)
    private String mrsd;

    @Column(name = "timescale_multiplier", nullable = true)
    private Double timescaleMultiplier;

    @Column(name = "output_filename", nullable = true)
    private String outputFilename;

    @Column(name = "geojson_filename", nullable = true)
    private String geojsonFilename;

    @Column(name = "ipfs_hash", nullable = true)
    private String ipfsHash;

    @Column(name = "has_external_annotations", nullable = true)
    private Boolean hasExternalAnnotations;

    @JsonManagedReference("tree-attributes")
    @OneToMany(mappedBy = "tree", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ContinuousAttributeEntity> attributes;

    @JsonManagedReference("tree-hpd-levels")
    @OneToMany(mappedBy = "tree", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<HpdLevelEntity> hpdLevels;

    @Enumerated(EnumType.STRING)
    private Status status;

    public ContinuousTreeModelEntity() {
    }

    public ContinuousTreeModelEntity(String sessionId, String treeFilename) {
        this.treeFilename = treeFilename;
        this.sessionId = sessionId;
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

    public Set<ContinuousAttributeEntity> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<ContinuousAttributeEntity> attributes) {
        this.attributes = attributes;
    }

    public Set<HpdLevelEntity> getHpdLevels() {
        return hpdLevels;
    }

    public void setHpdLevels(Set<HpdLevelEntity> hpdLevels) {
        this.hpdLevels = hpdLevels;
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

    public String getHpdLevel() {
        return hpdLevel;
    }

    public void setHpdLevel(String hpdLevel) {
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

    /**
     * @return the ipfsHash
     */
    public String getIpfsHash() {
        return ipfsHash;
    }

    /**
     * @param ipfsHash the ipfsHash to set
     */
    public void setIpfsHash(String ipfsHash) {
        this.ipfsHash = ipfsHash;
    }

    /**
     * @return the hasExternalAnnotations
     */
    public Boolean getHasExternalAnnotations() {
        return hasExternalAnnotations;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

}
