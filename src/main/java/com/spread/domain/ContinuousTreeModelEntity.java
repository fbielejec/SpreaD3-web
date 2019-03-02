package com.spread.domain;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "continuous_tree_models")
public class ContinuousTreeModelEntity {

    public enum Status  {
        EXCEPTION_OCCURED,
        GENERATING_OUTPUT, OUTPUT_READY,
        PUBLISHING_IPFS, IPFS_HASH_READY
    }

    @JsonIgnore
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
    private Integer hpdLevel;

    @Column(name = "mrsd", nullable = true)
    private String mrsd;

    @Column(name = "timescale_multiplier", nullable = true)
    private double timescaleMultiplier;

    @Column(name = "output_filename", nullable = true)
    private String outputFilename;

    @Column(name = "geojson_filename", nullable = true)
    private String geojsonFilename;

    @Column(name = "ipfs_hash", nullable = true)
    private String ipfsHash;

    @Column(name = "has_external_annotations", nullable = true)
    private Boolean hasExternalAnnotations;

    @JsonManagedReference("tree-attributes")
    @OneToMany(mappedBy = "tree", cascade = CascadeType.ALL)
    private Set<AttributeEntity> attributes;

    @JsonManagedReference("tree-hpd-levels")
    @OneToMany(mappedBy = "tree", cascade = CascadeType.ALL)
    private Set<HpdLevelEntity> hpdLevels;

    @Enumerated(EnumType.STRING)
    private Status status;

    public ContinuousTreeModelEntity() {
    }

    public ContinuousTreeModelEntity(String absolutePath, SessionEntity session) {
        this.treeFilename = absolutePath;
        this.session = session;
    }

    public ContinuousTreeModelEntity(SessionEntity session) {
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

    public Integer getHpdLevel() {
        return hpdLevel;
    }

    public void setHpdLevel(Integer hpdLevel) {
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
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the session
     */
    public SessionEntity getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(SessionEntity session) {
        this.session = session;
    }

    /**
     * @param timescaleMultiplier the timescaleMultiplier to set
     */
    public void setTimescaleMultiplier(double timescaleMultiplier) {
        this.timescaleMultiplier = timescaleMultiplier;
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
