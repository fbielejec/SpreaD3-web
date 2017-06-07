package com.spread.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "attributes")
public class AttributeEntity {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "name", nullable = false)
	private String name;

	@ManyToOne
	@JoinColumn(name = "fk_tree_id", nullable = false)
	private ContinuousTreeModelEntity tree;

	public AttributeEntity(String name, ContinuousTreeModelEntity tree) {
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

}
