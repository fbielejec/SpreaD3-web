package com.spread.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.spread.domain.ContinuousTreeModelEntity;

@Repository
public interface ContinuousTreeModelRepository extends JpaRepository<ContinuousTreeModelEntity, Integer> {

	 @Query("SELECT entity FROM ContinuousTreeModelEntity entity WHERE entity.treeFilename = ?1")
	 ContinuousTreeModelEntity findByTreeFilename(String filename);

}
