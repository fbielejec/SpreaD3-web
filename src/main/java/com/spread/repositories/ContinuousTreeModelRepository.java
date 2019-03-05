package com.spread.repositories;

import com.spread.domain.ContinuousTreeModelEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ContinuousTreeModelRepository extends JpaRepository<ContinuousTreeModelEntity, Integer> {

    @Query("SELECT entity FROM ContinuousTreeModelEntity entity WHERE entity.sessionId = ?1")
    ContinuousTreeModelEntity findBySessionId(String sessionId);

}
