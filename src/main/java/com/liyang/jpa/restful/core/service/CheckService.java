package com.liyang.jpa.restful.core.service;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.liyang.jpa.restful.core.annotation.JpaRestfulResource;
import com.liyang.jpa.restful.core.domain.BaseEntity;
import com.liyang.jpa.restful.core.event.RestfulEventListener;
import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.liyang.jpa.smart.query.exception.StructureException;

@Service
@DependsOn("entityRegister")
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
	    Map<String, RestfulEventListener> beansOfListener= applicationContext.getBeansOfType(RestfulEventListener.class);
		for (RestfulEventListener listener : beansOfListener.values()) {
			ResolvableType resolvableType = ResolvableType.forClass(listener.getClass());
			Class<?> entityClass = resolvableType.as(RestfulEventListener.class).getGeneric(0).resolve();
			JpaRestfulResource tableAnnotation = entityClass.getDeclaredAnnotation(JpaRestfulResource.class);
			if(tableAnnotation==null) {
				continue;
			}
			EntityStructure structure = SmartQuery.getStructure(entityClass);
			
			Set<String> events = structure.getEvents();
//			events.add("create");
//			events.add("linkCreate");
//			events.add("update");
//			events.add("linkUpdate");
//			events.add("delete");
//			events.add("linkDelete");
			
			Method[] declaredMethods = listener.getClass().getDeclaredMethods();
			for (Method method : declaredMethods) {
				if(method.getName().startsWith("on") && !method.getName().equals("onApplicationEvent")) {
					String substring = method.getName().substring(2);
					substring = substring.substring(0, 1).toLowerCase() + substring.substring(1);
					events.add(substring);
				}
			}
			
		}
	}
	
	

}
