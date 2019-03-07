package com.spread.repositories;

import com.spread.domain.ContinuousAttributeEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<ContinuousAttributeEntity, Integer> {

}
