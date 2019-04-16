package com.liyang.jpa.restful.core.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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

import com.liyang.jpa.restful.core.exception.AccessDeny403Exception;
import com.liyang.jpa.restful.core.exception.NotFound404Exception;
import com.liyang.jpa.restful.core.interceptor.JpaRestfulDeleteInterceptor;
import com.liyang.jpa.restful.core.response.HTTPPostOkResponse;
import com.liyang.jpa.restful.core.utils.CommonUtils;
import com.liyang.jpa.restful.core.utils.InterceptorComparator;
import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.ColumnJoinType;
import com.liyang.jpa.smart.query.db.structure.ColumnStucture;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;

@Service
public class DeleteService extends BaseService {
	private Map<String, JpaRestfulDeleteInterceptor> interceptors;
	protected final static Logger logger = LoggerFactory.getLogger(DeleteService.class);
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.interceptors = applicationContext.getBeansOfType(JpaRestfulDeleteInterceptor.class);
		this.applicationContext = applicationContext;
	}

	@Transactional(readOnly = false)
	public Object delete(String resource, String resourceId) {
		checkResource(resource, null);
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
		applyPreInterceptor(requestPath, oldInstance, context);
		publishEvent("delete",null,oldInstance);
		recursiveDelete(structure, oldInstance);

		HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
		httpPostOkResponse.setUuid(resourceId);
		return applyPostInterceptor(requestPath, httpPostOkResponse, context);
	}

	@Transactional(readOnly = false)
	public Object delete(String resource, String resourceId, String subResource, String subResourceId) {
		checkResource(resource, null);
		HashMap<String, Object> context = new HashMap<String, Object>();
		EntityStructure structure = SmartQuery.getStructure(resource);
		long fetchCount = SmartQuery.fetchCount(resource,
				"uuid=" + resourceId + "&" + subResource + ".uuid=" + subResourceId);
		if (fetchCount == 0) {
			throw new NotFound404Exception(subResource+":"+subResourceId);
		} else {
			Object owner;
			Optional ownerOptional = structure.getJpaRepository().findById(resourceId);
			owner = ownerOptional.get();
			
			
			EntityStructure subResourceStructure = SmartQuery
					.getStructure(CommonUtils.subResourceName(resource, subResource));
			Optional oldInstanceOptional = subResourceStructure.getJpaRepository().findById(subResourceId);
			Object subResourceObject = oldInstanceOptional.get();
			
			String requestPath = "/" + resource + "/" + resourceId + "/" + subResource + "/" + subResourceId;
			applyPreInterceptor(requestPath, subResourceObject, context);
			
			subDelete(structure, owner, subResource, subResourceStructure, subResourceObject);
			publishEvent("unlink",null,owner);
			publishEvent("unlink",null,subResourceObject);
			
			HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
			httpPostOkResponse.setUuid(subResourceId);
			return applyPostInterceptor(requestPath, httpPostOkResponse, context);
		}
	}

	private void subDelete(EntityStructure structure, Object owner, String subResource,
			EntityStructure subResourceStructure, Object subResourceObject) {
		ColumnStucture columnStucture = structure.getObjectFields().get(subResource);
		ColumnJoinType joinType = columnStucture.getJoinType();
		if (joinType.equals(ColumnJoinType.ONE_TO_ONE)) {
			if (columnStucture.getMappedBy() != null) {
				BeanWrapperImpl targetWrapper = new BeanWrapperImpl(subResourceObject);
				targetWrapper.setPropertyValue(columnStucture.getMappedBy(), null);
				subResourceStructure.getJpaRepository().save(subResourceObject);

			} else {
				BeanWrapperImpl ownerWrapper = new BeanWrapperImpl(owner);
				ownerWrapper.setPropertyValue(subResource, null);
				structure.getJpaRepository().save(owner);
			}
		} else if (joinType.equals(ColumnJoinType.MANY_TO_ONE)) {
			BeanWrapperImpl ownerWrapper = new BeanWrapperImpl(owner);
			ownerWrapper.setPropertyValue(subResource, null);
			structure.getJpaRepository().save(owner);
		} else if (joinType.equals(ColumnJoinType.ONE_TO_MANY)) {
			BeanWrapperImpl targetWrapper = new BeanWrapperImpl(subResourceObject);
			targetWrapper.setPropertyValue(columnStucture.getMappedBy(), null);
			subResourceStructure.getJpaRepository().save(subResourceObject);
		} else if (joinType.equals(ColumnJoinType.MANY_TO_MANY)) {
			if (columnStucture.getMappedBy() != null) {
				BeanWrapperImpl wrapper = new BeanWrapperImpl(subResourceObject);
				Object targetObject = wrapper.getPropertyValue(columnStucture.getMappedBy());
				Collection c = (Collection) targetObject;
				c.remove(owner);
				subResourceStructure.getJpaRepository().save(subResourceObject);
			} else {
				BeanWrapperImpl wrapper = new BeanWrapperImpl(owner);
				Object targetObject = wrapper.getPropertyValue(subResource);
				Collection c = (Collection) targetObject;
				c.remove(subResourceObject);
				structure.getJpaRepository().save(owner);
			}
		}

	}

	private void recursiveDelete(EntityStructure structure, Object oldInstance) {
		BeanWrapperImpl ownerWrapper = new BeanWrapperImpl(oldInstance);
		Set<Entry<String, ColumnStucture>> entrySet = structure.getObjectFields().entrySet();
		for (Entry<String, ColumnStucture> entry : entrySet) {
			deleteForeignKey(structure, oldInstance, entry.getKey());
		}
		structure.getJpaRepository().delete(oldInstance);
	}

	private void deleteForeignKey(EntityStructure structure, Object oldInstance, String key) {
		ColumnStucture columnStucture = structure.getObjectFields().get(key);
		ColumnJoinType joinType = columnStucture.getJoinType();
		Class<?> targetEntity = columnStucture.getTargetEntity();
		EntityStructure targetStructure = SmartQuery.getStructure(targetEntity);
		if (joinType.equals(ColumnJoinType.ONE_TO_ONE)) {
			if (columnStucture.getMappedBy() != null) {
				BeanWrapperImpl wrapper = new BeanWrapperImpl(oldInstance);
				Object targetObject = wrapper.getPropertyValue(key);
				if (targetObject != null) {
					BeanWrapperImpl targetWrapper = new BeanWrapperImpl(targetObject);
					targetWrapper.setPropertyValue(columnStucture.getMappedBy(), null);
					targetStructure.getJpaRepository().save(targetObject);
				}
			}
		} else if (joinType.equals(ColumnJoinType.ONE_TO_MANY)) {

			BeanWrapperImpl wrapper = new BeanWrapperImpl(oldInstance);
			Object targetObject = wrapper.getPropertyValue(key);
			if (targetObject != null) {
				Collection c = (Collection) targetObject;
				for (Object object : c) {
					BeanWrapperImpl targetWrapper = new BeanWrapperImpl(object);
					targetWrapper.setPropertyValue(columnStucture.getMappedBy(), null);
					targetStructure.getJpaRepository().save(object);
				}
			}
		} else if (joinType.equals(ColumnJoinType.MANY_TO_MANY)) {

			if (columnStucture.getMappedBy() != null) {
				BeanWrapperImpl wrapper = new BeanWrapperImpl(oldInstance);
				Object targetObject = wrapper.getPropertyValue(key);
				if (targetObject != null) {
					Collection c = (Collection) targetObject;
					for (Object object : c) {
						BeanWrapperImpl targetWrapper = new BeanWrapperImpl(object);
						Collection c2 = (Collection) targetWrapper.getPropertyValue(columnStucture.getMappedBy());
						c2.remove(oldInstance);
						targetStructure.getJpaRepository().save(object);
					}
				}
			} else {
				BeanWrapperImpl wrapper = new BeanWrapperImpl(oldInstance);
				wrapper.setPropertyValue(key, null);
				structure.getJpaRepository().save(oldInstance);
			}
		}
	}

	private boolean applyPreInterceptor(String requestPath, Object ownerInstance, Map<String, Object> context) {
		if (this.interceptors != null && this.interceptors.size() != 0) {

			PathMatcher matcher = new AntPathMatcher();

			Collection<JpaRestfulDeleteInterceptor> values = this.interceptors.values();
			JpaRestfulDeleteInterceptor[] interceptors = values.toArray(new JpaRestfulDeleteInterceptor[values.size()]);
			Arrays.sort(interceptors, new InterceptorComparator());
			// 顺序执行拦截器的preHandle方法，如果返回false,则调用triggerAfterCompletion方法
			for (int i = 0; i < interceptors.length; i++) {
				JpaRestfulDeleteInterceptor interceptor = interceptors[i];
				String[] patternPath = interceptor.path();
				boolean matched = false;
				for (String pattern : patternPath) {
					if (matcher.match(pattern, requestPath)) {
						matched = true;
					}
				}
				if (matched && !interceptor.preHandle(requestPath, ownerInstance, context)) {
					throw new AccessDeny403Exception("被拦截器["+interceptor.name()+"]拦截");
				}
			}
		}
		return true;
	}

	private HTTPPostOkResponse applyPostInterceptor(String requestPath, HTTPPostOkResponse httpPostOkResponse,
			Map<String, Object> context) {
		if (this.interceptors != null && this.interceptors.size() != 0) {

			PathMatcher matcher = new AntPathMatcher();

			Collection<JpaRestfulDeleteInterceptor> values = this.interceptors.values();
			JpaRestfulDeleteInterceptor[] interceptors = values.toArray(new JpaRestfulDeleteInterceptor[values.size()]);
			Arrays.sort(interceptors, new InterceptorComparator());
			for (int i = interceptors.length - 1; i >= 0; i--) {
				JpaRestfulDeleteInterceptor interceptor = interceptors[i];
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
	

}
