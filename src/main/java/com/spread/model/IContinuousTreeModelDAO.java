package com.spread.model;

public interface IContinuousTreeModelDAO {

	void persist(ContinuousTreeModelEntity entity);
	
	ContinuousTreeModelEntity retrieve(Long id);

	void update(Long id, ContinuousTreeModelEntity entity);
	
	void delete(ContinuousTreeModelEntity entity);

//	Session openCurrentSessionwithTransaction();
//
//	void closeCurrentSessionwithTransaction();

}
