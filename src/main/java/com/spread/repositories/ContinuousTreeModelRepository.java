package com.spread.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spread.domain.ContinuousTreeModelEntity;

@Repository
public interface ContinuousTreeModelRepository extends JpaRepository<ContinuousTreeModelEntity, Integer> {
}
