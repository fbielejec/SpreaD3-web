package com.spread.controller.bean;

import com.spread.model.ContinuousTreeModelDAO;
import com.spread.model.ContinuousTreeModelDTO;

public interface IContinuousTreeService {

	void persist(ContinuousTreeModelDTO dto);

	ContinuousTreeModelDTO retrieve(Long id);

	void update(ContinuousTreeModelDTO dto);

	void delete(ContinuousTreeModelDTO dto);

	void setDAO(ContinuousTreeModelDAO repository);
	
}
