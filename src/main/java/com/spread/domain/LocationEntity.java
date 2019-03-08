package com.spread.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations")
@Data @NoArgsConstructor  @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LocationEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "name", nullable = false)
    @EqualsAndHashCode.Include
    private String name;

    @Column(name = "latitude", nullable = false)
    @EqualsAndHashCode.Include
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    @EqualsAndHashCode.Include
    private Double longitude;

    @JsonBackReference("tree-locations")
    @ManyToOne
    @JoinColumn(name = "fk_tree_id", nullable = false)
    private DiscreteTreeModelEntity tree;

    public LocationEntity (String name, Double latitude, Double longitude) {
        this.name=name;
        this.latitude=latitude;
        this.longitude=longitude;
    }

}
