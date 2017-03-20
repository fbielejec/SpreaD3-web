package com.spread.services;

public class ContinuousTreeServiceBean implements IContinuousTreeService {

//	private ContinuousTreeModelDAO repository;

	public ContinuousTreeServiceBean() {
//		this.repository = new ContinuousTreeModelDAO();
	}
	
//	@Autowired
//	private ModelMapper modelMapper;

//	@Override
//	public void persist(ContinuousTreeModelDTO dto) {
//		ContinuousTreeModelEntity entity = convertToEntity(dto);
//		repository.openCurrentSessionwithTransaction();
//		repository.persist(entity);
//		repository.closeCurrentSessionwithTransaction();
//	}

//	@Override
//	public ContinuousTreeModelDTO retrieve(Long id) {
//		ContinuousTreeModelEntity entity = repository.retrieve(id);
//		ContinuousTreeModelDTO dto = entity.toDto();
//		return dto;
//	}

//	@Override
//	public void update(ContinuousTreeModelDTO dto) {
//		ContinuousTreeModelEntity entity = convertToEntity(dto);
//		repository.openCurrentSessionwithTransaction();
//		repository.delete(entity);
//		repository.closeCurrentSessionwithTransaction();
//	}

//	@Override
//	public void delete(ContinuousTreeModelDTO dto) {
//		ContinuousTreeModelEntity entity = convertToEntity(dto);
//		repository.openCurrentSessionwithTransaction();
//		repository.delete(entity);
//		repository.closeCurrentSessionwithTransaction();
//	}
	
//	private ContinuousTreeModelEntity convertToEntity(ContinuousTreeModelDTO dto) {
//		ContinuousTreeModelEntity entity = modelMapper.map(dto, ContinuousTreeModelEntity.class);
//		return entity;
//	}

	/**
	 * Used for testing only
	 * 
	 * @param dao : mock of the DAO
	 * */
//	@Override
//	public void setDAO(ContinuousTreeModelDAO dao) {
//		this.repository = dao;
//	}
	
}
