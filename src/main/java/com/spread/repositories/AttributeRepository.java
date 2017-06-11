package com.spread.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spread.domain.AttributeEntity;

public interface AttributeRepository extends JpaRepository<AttributeEntity, Integer> {

}
