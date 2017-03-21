package com.spread.configuration;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.spread.domain.ContinuousTreeModelEntity;
import com.spread.repositories.ContinuousTreeModelRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = ContinuousTreeModelRepository.class)
public class RepositoryConfiguration {

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUrl("jdbc:h2:mem:AZ;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		return dataSource;
	}

	@Bean
	public EntityManager entityManager() {
		return entityManagerFactory().getObject().createEntityManager();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

		HibernateJpaVendorAdapter persistenceProvider = new HibernateJpaVendorAdapter();
		persistenceProvider.setGenerateDdl(true);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setDataSource(dataSource());
		factory.setPackagesToScan(ContinuousTreeModelEntity.class.getPackage().getName());
		factory.setJpaVendorAdapter(persistenceProvider);
		factory.afterPropertiesSet();
		return factory;
	}

//	@Bean 
//	public StorageService continuousTreeService() {
//		return new FileSystemStorageService(new StorageProperties());
//	}
	
	
	
	
}
