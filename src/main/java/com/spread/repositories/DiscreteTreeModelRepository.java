package com.spread.repositories;

import com.spread.domain.ContinuousTreeModelEntity;
import com.spread.domain.DiscreteTreeModelEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscreteTreeModelRepository extends JpaRepository<ContinuousTreeModelEntity, Integer> {

    @Query("SELECT entity FROM DiscreteTreeModelEntity entity WHERE entity.sessionId = ?1")
    DiscreteTreeModelEntity findBySessionId(String sessionId);

}
