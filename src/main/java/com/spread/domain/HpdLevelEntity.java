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

@Entity
@Table(name = "hpd_levels")
public class HpdLevelEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @JsonBackReference("tree-hpd-levels")
    @ManyToOne
    @JoinColumn(name = "fk_tree_id", nullable = false)
    private ContinuousTreeModelEntity tree;

    public HpdLevelEntity() {
    }

    public HpdLevelEntity(String name, ContinuousTreeModelEntity tree) {
        this.name = name;
        this.tree = tree;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContinuousTreeModelEntity getTree() {
        return tree;
    }

    public void setTree(ContinuousTreeModelEntity tree) {
        this.tree = tree;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((tree == null) ? 0 : tree.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HpdLevelEntity other = (HpdLevelEntity) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (tree == null) {
            if (other.tree != null)
                return false;
        } else if (!tree.equals(other.tree))
            return false;
        return true;
    }

}
