package com.spread.repositories;

import javax.transaction.Transactional;

import com.spread.domain.DiscreteTreeModelEntity;
import com.spread.domain.LocationEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, Integer> {

    @Transactional
    void deleteLocationsByTree(DiscreteTreeModelEntity tree);

}
