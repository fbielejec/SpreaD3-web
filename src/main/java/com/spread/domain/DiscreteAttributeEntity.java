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
@Table(name = "discrete_attributes")
@Data @NoArgsConstructor @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DiscreteAttributeEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "name", nullable = false)
    @EqualsAndHashCode.Include
    private String name;

    @JsonBackReference("tree-attributes")
    @ManyToOne
    @JoinColumn(name = "fk_tree_id", nullable = false)
    private DiscreteTreeModelEntity tree;

    public DiscreteAttributeEntity(String name, DiscreteTreeModelEntity tree) {
        this.name = name;
        this.tree = tree;
    }

}