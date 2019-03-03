package com.liyang.jpa.restful.core.service;

import java.util.Map;

import javax.persistence.Table;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.liyang.jpa.mysql.config.ApplicationContextSupport;
import com.liyang.jpa.mysql.exception.StructureException;
import com.liyang.jpa.restful.core.annotation.JpaRestfulResource;
import com.liyang.jpa.restful.core.domain.BaseEntity;

@Service
public class CheckService implements ApplicationContextAware,InitializingBean  {

	private ApplicationContext applicationContext;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, JpaRepository> beans = applicationContext.getBeansOfType(JpaRepository.class);
		for (JpaRepository jpaRepository : beans.values()) {
			ResolvableType resolvableType = ResolvableType.forClass(jpaRepository.getClass());
			Class<?> entityClass = resolvableType.as(JpaRepository.class).getGeneric(0).resolve();
			JpaRestfulResource tableAnnotation = entityClass.getDeclaredAnnotation(JpaRestfulResource.class);
			if(tableAnnotation!=null) {
				boolean assignableFrom = BaseEntity.class.isAssignableFrom(entityClass);
				if(!assignableFrom) {
					throw new StructureException(entityClass.getSimpleName()+"没有实现BaseEntity接口");
				}
			}
		}
		
	}
	
	

}
