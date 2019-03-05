package com.spread.repositories;

import com.spread.domain.KeyEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyRepository extends JpaRepository<KeyEntity, Integer> {

    KeyEntity findFirstByOrderByIdDesc();

}
