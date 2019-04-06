package com.liyang.jpa.restful.core.service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liyang.jpa.restful.core.event.RestfulEvent;
import com.liyang.jpa.restful.core.exception.AccessDeny403Exception;
import com.liyang.jpa.restful.core.exception.Business503Exception;
import com.liyang.jpa.restful.core.exception.JsonFormat406Exception;
import com.liyang.jpa.restful.core.exception.NotFound404Exception;
import com.liyang.jpa.restful.core.exception.ServerError500Exception;
import com.liyang.jpa.restful.core.exception.Validator422Exception;
import com.liyang.jpa.restful.core.interceptor.JpaRestfulPostInterceptor;
import com.liyang.jpa.restful.core.response.HTTPPostOkResponse;
import com.liyang.jpa.restful.core.utils.CommonUtils;
import com.liyang.jpa.restful.core.utils.InterceptorComparator;
import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.ColumnJoinType;
import com.liyang.jpa.smart.query.db.structure.ColumnStucture;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;

@Service
public class PostService extends BaseService {
	private Map<String, JpaRestfulPostInterceptor> interceptors;
	private ApplicationContext applicationContext;
	protected final static Logger logger = LoggerFactory.getLogger(PostService.class);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.interceptors = applicationContext.getBeansOfType(JpaRestfulPostInterceptor.class);
		this.applicationContext = applicationContext;
	}
	
	
	@Transactional(readOnly = false)
	public Object create(String resource, String body) {
		checkResource(resource, null);
		Map<String, Object> bodyToMap = bodyToMap(body);
		HashMap<String, Object> context = new HashMap<String, Object>();
		String requestPath = "/" + resource;
		applyPreInterceptor(requestPath, bodyToMap, null, context);

		EntityStructure structure = SmartQuery.getStructure(resource);
		Object readObject = withoutIdBodyValidationMap(structure, bodyToMap);
		Object save = structure.getJpaRepository().saveAndFlush(readObject);
		
		publish("create",bodyToMap,save,structure);
		BeanWrapperImpl saveImpl = new BeanWrapperImpl(save);
		Object savedUUID = saveImpl.getPropertyValue("uuid");
		HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
		httpPostOkResponse.setUuid(savedUUID.toString());
		return applyPostInterceptor(requestPath, httpPostOkResponse, context);
	}



	@Transactional(readOnly = false)
	public Object create(String resource, String resourceId, String subResource, String body) {
		checkResource(resource, null);
		Map<String, Object> bodyToMap = bodyToMap(body);
		HashMap<String, Object> context = new HashMap<String, Object>();
		EntityStructure structure = SmartQuery.getStructure(resource);
		Object owner;
		Optional ownerOptional = structure.getJpaRepository().findById(resourceId);
		if (!ownerOptional.isPresent()) {
			throw new NotFound404Exception(resource+":"+resourceId);
		} else {
			owner = ownerOptional.get();
		}
		String requestPath = "/" + resource + "/" + resourceId + "/" + subResource;
		applyPreInterceptor(requestPath, bodyToMap, owner, context);

		HTTPPostOkResponse httpPostOkResponse = subResourceCreate(structure, owner, subResource, bodyToMap);
		
		publish("update",bodyToMap,owner,structure);
		
		return applyPostInterceptor(requestPath, httpPostOkResponse, context);
	}

	@Transactional(readOnly = false)
	public Object create(String resource, String resourceId, String subResource, String subResourceId,
			String subsubResource, String body) {
		checkSubResource(resource, subResource, null);
		Map<String, Object> bodyToMap = bodyToMap(body);
		HashMap<String, Object> context = new HashMap<String, Object>();
		long fetchCount = SmartQuery.fetchCount(resource,
				"uuid=" + resourceId + "&" + subResource + ".uuid=" + subResourceId);
		if (fetchCount == 0) {
			throw new NotFound404Exception(subResource+":"+subResourceId);
		}
		String requestPath = "/" + resource + "/" + resourceId + "/" + subResource + "/" + subResourceId + "/"
				+ subsubResource;

		String subResourceName = CommonUtils.subResourceName(resource, subResource);
		EntityStructure subStructure = SmartQuery.getStructure(subResourceName);
		Optional ownerOptional = subStructure.getJpaRepository().findById(subResourceId);
		Object owner = ownerOptional.get();
		
		applyPreInterceptor(requestPath, bodyToMap, owner, context);
		
		HTTPPostOkResponse httpPostOkResponse = subResourceCreate(subStructure, owner, subsubResource, bodyToMap);
		
		publish("update",bodyToMap,owner,subStructure);
		
		return applyPostInterceptor(requestPath, httpPostOkResponse, context);
	}

	@Transactional(readOnly = false)
	public Object update(String resource, String resourceId, String body) {
		checkResource(resource, null);
		Map<String, Object> bodyToMap = bodyToMap(body);
		HashMap<String, Object> context = new HashMap<String, Object>();
		EntityStructure structure = SmartQuery.getStructure(resource);
		Object oldInstance;
		Optional oldInstanceOptional = structure.getJpaRepository().findById(resourceId);
		if (!oldInstanceOptional.isPresent()) {
			throw new NotFound404Exception(resource+":"+resourceId);
		} else {
			oldInstance = oldInstanceOptional.get();
		}
		String requestPath = "/" + resource + "/" + resourceId;
		applyPreInterceptor(requestPath, bodyToMap, oldInstance, context);

		Object newInstance = bodyValidation(structure, bodyToMap, oldInstance);
		
		Object saveAndFlush = structure.getJpaRepository().saveAndFlush(newInstance);
		
		HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
		httpPostOkResponse.setUuid(resourceId);
		return applyPostInterceptor(requestPath, httpPostOkResponse, context);
	}

	@Transactional(readOnly = false)
	public Object update(String resource, String resourceId, String subResource, String subResourceId, String body) {
		checkSubResource(resource, subResource, null);
		Map<String, Object> bodyToMap = bodyToMap(body);
		HashMap<String, Object> context = new HashMap<String, Object>();
		EntityStructure structure = SmartQuery.getStructure(resource);
		long fetchCount = SmartQuery.fetchCount(resource,
				"uuid=" + resourceId + "&" + subResource + ".uuid=" + subResourceId);
		if (fetchCount == 0) {
			throw new NotFound404Exception(subResource+":"+subResourceId);
		} else {
			EntityStructure subResourceStructure = SmartQuery.getStructure(CommonUtils.subResourceName(resource, subResource));
			Optional oldInstanceOptional = subResourceStructure.getJpaRepository().findById(subResourceId);
			Object oldInstance = oldInstanceOptional.get();
			String requestPath = "/" + resource + "/" + resourceId + "/" + subResource + "/" + subResourceId;
			applyPreInterceptor(requestPath, bodyToMap, oldInstance, context);

			Object newInstance = bodyValidation(subResourceStructure, bodyToMap, oldInstance);
			Object saveAndFlush = subResourceStructure.getJpaRepository().saveAndFlush(newInstance);
			HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
			httpPostOkResponse.setUuid(subResourceId);
			return applyPostInterceptor(requestPath, httpPostOkResponse, context);
		}
	}

	private HTTPPostOkResponse subResourceCreate(EntityStructure structure, Object owner, String subResource,
			Map<String, Object> bodyToMap) {

		HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
		BeanWrapperImpl ownerWrapper = new BeanWrapperImpl(owner);
		String ownerId = ownerWrapper.getPropertyValue("uuid").toString();
		ColumnStucture columnStucture = structure.getObjectFields().get(subResource);
		EntityStructure targetEntityStructure = SmartQuery.getStructure(columnStucture.getTargetEntity());
		ObjectMapper mapper = new ObjectMapper();
		Object readObject = null;
		try {
			readObject = CommonUtils.mapToObject(bodyToMap, targetEntityStructure.getEntityClass());
		} catch (IOException e) {
			e.printStackTrace();
			throw new JsonFormat406Exception("json转成对象错误");
		}
		BeanWrapperImpl wrapper = new BeanWrapperImpl(readObject);
		Object bodyId = wrapper.getPropertyValue("uuid");
		ColumnJoinType joinType = columnStucture.getJoinType();

		Object retUuid = null;

		if (joinType.equals(ColumnJoinType.ONE_TO_ONE)) {
			Object existSubResourceObject = ownerWrapper.getPropertyValue(subResource);
			if (existSubResourceObject != null) {
				throw new ServerError500Exception(subResource+"子资源已经存在");
			}
			Object bodyObject = withoutIdBodyValidationObject(targetEntityStructure, readObject);
			Object save;
			if (columnStucture.getMappedBy() != null) {
				BeanWrapperImpl bodyObjectWrapper = new BeanWrapperImpl(bodyObject);
				bodyObjectWrapper.setPropertyValue(columnStucture.getMappedBy(), owner);
				save = targetEntityStructure.getJpaRepository().saveAndFlush(bodyObject);
				BeanWrapperImpl saveWrapper = new BeanWrapperImpl(save);
				retUuid = saveWrapper.getPropertyValue("uuid");

			} else {
				save = targetEntityStructure.getJpaRepository().saveAndFlush(bodyObject);
				BeanWrapperImpl saveWrapper = new BeanWrapperImpl(save);
				retUuid = saveWrapper.getPropertyValue("uuid");
				ownerWrapper.setPropertyValue(subResource, save);
				structure.getJpaRepository().saveAndFlush(owner);

			}
			publish("create",bodyToMap,save,targetEntityStructure);
			
		} else if (joinType.equals(ColumnJoinType.ONE_TO_MANY)) {

			Object bodyObject = withoutIdBodyValidationObject(targetEntityStructure, readObject);
			BeanWrapperImpl bodyObjectWrapper = new BeanWrapperImpl(bodyObject);
			bodyObjectWrapper.setPropertyValue(columnStucture.getMappedBy(), owner);
			Object save = targetEntityStructure.getJpaRepository().saveAndFlush(bodyObject);
			BeanWrapperImpl saveWrapper = new BeanWrapperImpl(save);
			retUuid = saveWrapper.getPropertyValue("uuid");
			publish("create",bodyToMap,save,targetEntityStructure);

		} else if (joinType.equals(ColumnJoinType.MANY_TO_ONE)) {
			if (bodyId == null) {
				throw new ServerError500Exception("MANY_TO_ONE结构体只能关联，不允许创建");
			}
			Optional subResourceOptional = targetEntityStructure.getJpaRepository().findById(bodyId);
			if (!subResourceOptional.isPresent()) {
				throw new NotFound404Exception(targetEntityStructure.getName()+":"+bodyId);
			}

			ownerWrapper.setPropertyValue(subResource, subResourceOptional.get());
			Object save = structure.getJpaRepository().saveAndFlush(owner);
			retUuid = bodyId;
			publish("link",bodyToMap,subResourceOptional.get(),targetEntityStructure);

		} else if (joinType.equals(ColumnJoinType.MANY_TO_MANY)) {
			if (bodyId == null) {
				throw new ServerError500Exception("MANY_TO_MANY结构体只能关联，不允许创建");
			}
			Optional subResourceOptional = targetEntityStructure.getJpaRepository().findById(bodyId);
			if (!subResourceOptional.isPresent()) {
				throw new NotFound404Exception(targetEntityStructure.getName()+":"+bodyId);
			}
			Object newSubResource = subResourceOptional.get();
			
			if (columnStucture.getMappedBy() != null) {
				BeanWrapperImpl newSubResourceWrapper = new BeanWrapperImpl(newSubResource);
				Object propertyValue = newSubResourceWrapper.getPropertyValue(columnStucture.getMappedBy());
				((Set) propertyValue).add(owner);
				Object saveAndFlush = targetEntityStructure.getJpaRepository().saveAndFlush(newSubResource);
			} else {
				Object propertyValue = ownerWrapper.getPropertyValue(subResource);
				((Set) propertyValue).add(newSubResource);
				Object saveAndFlush = structure.getJpaRepository().saveAndFlush(owner);
			}
			retUuid = bodyId;
			publish("link",bodyToMap,newSubResource,targetEntityStructure);
		}
		httpPostOkResponse.setUuid(retUuid.toString());
		return httpPostOkResponse;
	}

	private Object withoutIdBodyValidationObject(EntityStructure structure, Object body) {

		BeanWrapperImpl readWrapper = new BeanWrapperImpl(body);
		Object uuid = readWrapper.getPropertyValue("uuid");
		if (uuid != null) {
			throw new JsonFormat406Exception("创建对象不允许带uuid");
		}
		Map<String, String> validate = CommonUtils.validate(body);
		if (!validate.isEmpty()) {

			throw new Validator422Exception(validate);
		}
		return body;

	}

	private Object withoutIdBodyValidationMap(EntityStructure structure, Map<String, Object> bodyToMap) {

		try {
			Object readObject = CommonUtils.mapToObject(bodyToMap, structure.getEntityClass());
			return withoutIdBodyValidationObject(structure,readObject);
		} catch (IOException e) {
			e.printStackTrace();
			throw new JsonFormat406Exception("json转成对象错误");
		}
	}
	private Object bodyValidation(EntityStructure structure, Map<String, Object> body, Object old) {
		try {
			
			BeanWrapperImpl beanWrapperImpl = new BeanWrapperImpl(old);
			String uuid = beanWrapperImpl.getPropertyValue("uuid").toString();
			body.put("uuid", uuid);
			Object readObject = CommonUtils.mapToObject(body, structure.getEntityClass());
			publish("update",body,readObject,structure);
			
			CommonUtils.copyPropertiesIgnoreNull(readObject, old);
			Map<String, String> validate = CommonUtils.validate(old);
			if (!validate.isEmpty()) {
				throw new Validator422Exception(validate);
			}
			return old;
		} catch (IOException e) {
			e.printStackTrace();
			throw new JsonFormat406Exception("json转成对象错误");
		}
	}

	private boolean applyPreInterceptor(String requestPath, Map<String, Object> body, Object ownerInstance,
			Map<String, Object> context) {
		if (this.interceptors != null && this.interceptors.size() != 0) {

			PathMatcher matcher = new AntPathMatcher();

			Collection<JpaRestfulPostInterceptor> values = this.interceptors.values();
			JpaRestfulPostInterceptor[] interceptors = values.toArray(new JpaRestfulPostInterceptor[values.size()]);
			Arrays.sort(interceptors, new InterceptorComparator());
			// 顺序执行拦截器的preHandle方法，如果返回false,则调用triggerAfterCompletion方法
			for (int i = 0; i < interceptors.length; i++) {
				JpaRestfulPostInterceptor interceptor = interceptors[i];

				String[] patternPath = interceptor.path();
				boolean matched = false;
				for (String pattern : patternPath) {
					if (matcher.match(pattern, requestPath)) {
						matched = true;
					}
				}
				if (matched && !interceptor.preHandle(requestPath, body, ownerInstance, context)) {
					throw new AccessDeny403Exception("数据被拦截器"+interceptor.name()+"拦截");
				}
			}
		}
		return true;
	}

	private Object applyPostInterceptor(String requestPath, HTTPPostOkResponse httpPostOkResponse,
			Map<String, Object> context) {
		if (this.interceptors != null && this.interceptors.size() != 0) {

			PathMatcher matcher = new AntPathMatcher();

			Collection<JpaRestfulPostInterceptor> values = this.interceptors.values();
			JpaRestfulPostInterceptor[] interceptors = values.toArray(new JpaRestfulPostInterceptor[values.size()]);
			Arrays.sort(interceptors, new InterceptorComparator());
			for (int i = interceptors.length - 1; i >= 0; i--) {
				JpaRestfulPostInterceptor interceptor = interceptors[i];
				String[] patternPath = interceptor.path();
				boolean matched = false;
				for (String pattern : patternPath) {
					if (matcher.match(pattern, requestPath)) {
						matched = true;
					}
				}
				if (matched) {
					httpPostOkResponse = interceptor.postHandle(requestPath, httpPostOkResponse, context);
				}
			}
		}
		return httpPostOkResponse;

	}
	
	private Map<String, Object> bodyToMap(String body) {
		Map<String, Object> bodyToMap = null;
		try {
			bodyToMap = CommonUtils.stringToMap(body);
		} catch (IOException e) {
			e.printStackTrace();
			throw new JsonFormat406Exception("json解析错误");
		}
		return bodyToMap;
	}
	
	private void publish(String defaultEvent, Map<String, Object> bodyToMap, Object owner,  EntityStructure ownerStructure) {
		if(bodyToMap.containsKey("event")) {
			defaultEvent = bodyToMap.get("event").toString();
		}
		 applicationContext.publishEvent(new RestfulEvent(defaultEvent,owner,ownerStructure));
	
	}

}
