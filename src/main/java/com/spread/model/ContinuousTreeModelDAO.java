package com.spread.model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class ContinuousTreeModelDAO implements IContinuousTreeModelDAO {

	private Session currentSession;
	private Transaction currentTransaction;

	@Override
	public void persist(ContinuousTreeModelEntity entity) {
		getCurrentSession().save(entity);
	}

	@Override
	public ContinuousTreeModelEntity retrieve(Long id) {
		ContinuousTreeModelEntity entity = (ContinuousTreeModelEntity) getCurrentSession()
				.get(ContinuousTreeModelEntity.class, id);
		return entity;
	}

	@Override
	public void update(Long id, ContinuousTreeModelEntity entity) {
		getCurrentSession().update(entity);
	}

	@Override
	public void delete(ContinuousTreeModelEntity entity) {
		getCurrentSession().delete(entity);
	}
	
	public Session openCurrentSession() {
		currentSession = getSessionFactory().openSession();
		return currentSession;
	}

	public Session openCurrentSessionwithTransaction() {
		currentSession = getSessionFactory().openSession();
		currentTransaction = currentSession.beginTransaction();
		return currentSession;
	}

	public void closeCurrentSession() {
		currentSession.close();
	}

	public void closeCurrentSessionwithTransaction() {
		currentTransaction.commit();
		currentSession.close();
	}

	private static SessionFactory getSessionFactory() {
		Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
		StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties());
		SessionFactory sessionFactory = configuration.buildSessionFactory(builder.build());
		return sessionFactory;
	}

	public Session getCurrentSession() {
		return currentSession;
	}

	public void setCurrentSession(Session currentSession) {
		this.currentSession = currentSession;
	}

	public Transaction getCurrentTransaction() {
		return currentTransaction;
	}

	public void setCurrentTransaction(Transaction currentTransaction) {
		this.currentTransaction = currentTransaction;
	}

}
