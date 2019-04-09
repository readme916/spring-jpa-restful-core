package com.liyang.jpa.restful.core.service;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.liyang.jpa.restful.core.annotation.AllowCondition;
import com.liyang.jpa.restful.core.annotation.AllowFields;
import com.liyang.jpa.restful.core.annotation.ForbidFields;
import com.liyang.jpa.restful.core.annotation.JpaRestfulResource;
import com.liyang.jpa.restful.core.domain.BaseEntity;
import com.liyang.jpa.restful.core.event.EventManager;
import com.liyang.jpa.restful.core.utils.CommonUtils;
import com.liyang.jpa.restful.core.utils.EntityStructureEx;
import com.liyang.jpa.restful.core.utils.EntityStructureEx.EntityEvent;
import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.liyang.jpa.smart.query.exception.StructureException;

@Service
@DependsOn("entityRegister")
public class CheckService implements ApplicationContextAware,InitializingBean  {

	private ApplicationContext applicationContext;
	
	public static HashMap<String, EntityStructureEx> nameToStructure = new HashMap();

	public static HashMap<Class<?>, EntityStructureEx> classToStructure = new HashMap();
	
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		HashMap<Class<?>,EntityStructure> classtostructure2 = SmartQuery.getClasstostructure();
		HashMap<String,EntityStructure> nametostructure2 = SmartQuery.getNametostructure();
		
		Set<Entry<String,EntityStructure>> entrySet = nametostructure2.entrySet();
		for (Entry<String, EntityStructure> entry : entrySet) {
			EntityStructureEx entityStructureEx = new EntityStructureEx();
			BeanUtils.copyProperties(entry.getValue(), entityStructureEx);
			nameToStructure.put(entry.getKey(), entityStructureEx);
			classToStructure.put(entry.getValue().getEntityClass(), entityStructureEx);
		}
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
	    Map<String, EventManager> eventManagers= applicationContext.getBeansOfType(EventManager.class);
		for (EventManager manager : eventManagers.values()) {
			ResolvableType resolvableType = ResolvableType.forClass(manager.getClass());
			Class<?> entityClass = resolvableType.as(EventManager.class).getGeneric(0).resolve();
			EntityStructureEx structure = CommonUtils.getStructure(entityClass);
			if(structure.getEventManager()!=null) {
				throw new StructureException(entityClass.getSimpleName()+"不允许多个事件管理器");
			}else {
				structure.setEventManager(manager);
			}
			
			//分析事件管理器内部定义事件
			HashSet<EntityEvent> events = structure.getEvents();
			Method[] declaredMethods = manager.getClass().getDeclaredMethods();
			for (Method method : declaredMethods) {
				if(method.getName().startsWith("on")) {
					
					EntityEvent entityEvent = new EntityEvent();
					
					String eventName = method.getName().substring(2);
					eventName = eventName.substring(0, 1).toLowerCase() + eventName.substring(1);
					entityEvent.setName(eventName);
					
					HashSet<String> hashSet = new HashSet<String>();
					hashSet.addAll(structure.getSimpleFields().keySet());
					hashSet.remove("createdBy");
					hashSet.remove("createdAt");
					hashSet.remove("modifiedAt");
					hashSet.remove("modifiedBy");
					
					AllowFields allowAnnotation = method.getAnnotation(AllowFields.class);
					if (allowAnnotation != null) {
						entityEvent.setFields(new HashSet<String>(Arrays.asList(allowAnnotation.value())));
					} else {
						ForbidFields forbidAnnotation = method.getAnnotation(ForbidFields.class);
						if (forbidAnnotation != null) {
							hashSet.removeAll(Arrays.asList(forbidAnnotation.value()));
							entityEvent.setFields(hashSet);
						}else {
							entityEvent.setFields(hashSet);
						}
					}
					AllowCondition allowCondition = method.getAnnotation(AllowCondition.class);
					if (allowCondition != null) {
						entityEvent.setCondition(allowCondition.value());
					}
					
					events.add(entityEvent);
				}
			}
			
		}
	}
	
	

}
