package com.spread.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spread.domain.KeyEntity;

public interface KeyRepository extends JpaRepository<KeyEntity, Integer> {

//	@Query("SELECT entity FROM KeyEntity entity ORDER BY entity.id DESC")
	KeyEntity findFirstByOrderByIdDesc();

}
