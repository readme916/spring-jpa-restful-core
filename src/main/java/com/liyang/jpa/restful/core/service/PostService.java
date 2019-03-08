package com.liyang.jpa.restful.core.service;

import java.io.IOException;
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
import com.liyang.jpa.mysql.config.JpaSmartQuerySupport;
import com.liyang.jpa.mysql.db.SmartQuery;
import com.liyang.jpa.mysql.db.structure.ColumnJoinType;
import com.liyang.jpa.mysql.db.structure.ColumnStucture;
import com.liyang.jpa.mysql.db.structure.EntityStructure;
import com.liyang.jpa.restful.core.exception.BusinessException;
import com.liyang.jpa.restful.core.exception.PostFormatException;
import com.liyang.jpa.restful.core.interceptor.JpaRestfulPostInterceptor;
import com.liyang.jpa.restful.core.response.HTTPPostOkResponse;
import com.liyang.jpa.restful.core.utils.CommonUtils;
import com.liyang.jpa.restful.core.utils.InterceptorComparator;

@Service
public class PostService extends BaseService {
	private Map<String, JpaRestfulPostInterceptor> interceptors;
	protected final static Logger logger = LoggerFactory.getLogger(PostService.class);

	@Transactional(readOnly = false)
	public Object create(String resource, String body) {
		checkResource(resource, null);
		Map<String, Object> bodyToMap=null;
		try {
			bodyToMap = CommonUtils.stringToMap(body);
		} catch (IOException e) {
			e.printStackTrace();
			throw new PostFormatException(3281, "数据格式异常", "json解析错误");
		}
		HashMap<Object, Object> context = new HashMap<Object,Object>();
		String requestPath = "/" + resource;
		applyPreInterceptor(requestPath, bodyToMap, null, context);

		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		Object readObject = withoutIdBodyValidation(structure, bodyToMap);
		Object save = structure.getJpaRepository().saveAndFlush(readObject);
		
		BeanWrapperImpl saveImpl = new BeanWrapperImpl(save);
		Object savedUUID = saveImpl.getPropertyValue("uuid");
		HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
		httpPostOkResponse.setUuid(savedUUID.toString());
		return applyPostInterceptor(requestPath, httpPostOkResponse, context);
	}

	@Transactional(readOnly = false)
	public Object create(String resource, String resourceId, String subResource, String body) {
		checkResource(resource, null);
		Map<String, Object> bodyToMap=null;
		try {
			bodyToMap = CommonUtils.stringToMap(body);
		} catch (IOException e) {
			e.printStackTrace();
			throw new PostFormatException(3282, "数据格式异常", "json解析错误");
		}
		HashMap<Object, Object> context = new HashMap<Object,Object>();
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		Object owner;
		Optional ownerOptional = structure.getJpaRepository().findById(resourceId);
		if (!ownerOptional.isPresent()) {
			throw new PostFormatException(3100, "数据不存在", resourceId);
		} else {
			owner = ownerOptional.get();
		}
		String requestPath = "/" + resource + "/" + resourceId + "/" + subResource;
		applyPreInterceptor(requestPath, bodyToMap, null, context);

		HTTPPostOkResponse httpPostOkResponse = subResourceCreate(structure, owner, subResource, bodyToMap);

		return applyPostInterceptor(requestPath, httpPostOkResponse, context);
	}

	@Transactional(readOnly = false)
	public Object create(String resource, String resourceId, String subResource, String subResourceId,
			String subsubResource, String body) {
		checkSubResource(resource, subResource, null);
		Map<String, Object> bodyToMap=null;
		try {
			bodyToMap = CommonUtils.stringToMap(body);
		} catch (IOException e) {
			e.printStackTrace();
			throw new PostFormatException(3283, "数据格式异常", "json解析错误");
		}
		HashMap<Object, Object> context = new HashMap<Object,Object>();
		long fetchCount = SmartQuery.fetchCount(resource,
				"uuid=" + resourceId + "&" + subResource + ".uuid=" + subResourceId);
		if (fetchCount == 0) {
			throw new PostFormatException(3530, "数据不存在", "");
		}
		String requestPath = "/" + resource + "/" + resourceId + "/" + subResource + "/" + subResourceId + "/"
				+ subsubResource;
		applyPreInterceptor(requestPath, bodyToMap, null, context);

		String subResourceName = CommonUtils.subResourceName(resource, subResource);
		EntityStructure subStructure = JpaSmartQuerySupport.getStructure(subResourceName);
		Optional ownerOptional = subStructure.getJpaRepository().findById(subResourceId);
		Object owner = ownerOptional.get();
		HTTPPostOkResponse httpPostOkResponse = subResourceCreate(subStructure, owner, subsubResource, bodyToMap);
		return applyPostInterceptor(requestPath, httpPostOkResponse, context);
	}

	@Transactional(readOnly = false)
	public Object update(String resource, String resourceId, String body) {
		checkResource(resource, null);
		Map<String, Object> bodyToMap=null;
		try {
			bodyToMap = CommonUtils.stringToMap(body);
		} catch (IOException e) {
			e.printStackTrace();
			throw new PostFormatException(3284, "数据格式异常", "json解析错误");
		}
		HashMap<Object, Object> context = new HashMap<Object,Object>();
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		Object oldInstance;
		Optional oldInstanceOptional = structure.getJpaRepository().findById(resourceId);
		if (!oldInstanceOptional.isPresent()) {
			throw new PostFormatException(3100, "数据不存在", "");
		} else {
			oldInstance = oldInstanceOptional.get();
		}
		String requestPath = "/" + resource + "/" + resourceId;
		applyPreInterceptor(requestPath, bodyToMap, oldInstance, context);

		Object newInstance = bodyValidation(structure, bodyToMap, oldInstance);

		structure.getJpaRepository().saveAndFlush(newInstance);
		HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
		httpPostOkResponse.setUuid(resourceId);
		return applyPostInterceptor(requestPath, httpPostOkResponse, context);
	}

	@Transactional(readOnly = false)
	public Object update(String resource, String resourceId, String subResource, String subResourceId, String body) {
		checkSubResource(resource, subResource, null);
		Map<String, Object> bodyToMap=null;
		try {
			bodyToMap = CommonUtils.stringToMap(body);
		} catch (IOException e) {
			e.printStackTrace();
			throw new PostFormatException(3285, "数据格式异常", "json解析错误");
		}
		HashMap<Object, Object> context = new HashMap<Object,Object>();
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		long fetchCount = SmartQuery.fetchCount(resource,
				"uuid=" + resourceId + "&" + subResource + ".uuid=" + subResourceId);
		if (fetchCount == 0) {
			throw new PostFormatException(3330, "数据不存在", "");
		} else {
			EntityStructure subResourceStructure = JpaSmartQuerySupport
					.getStructure(CommonUtils.subResourceName(resource, subResource));
			Optional oldInstanceOptional = subResourceStructure.getJpaRepository().findById(subResourceId);
			Object oldInstance = oldInstanceOptional.get();
			String requestPath = "/" + resource + "/" + resourceId + "/" + subResource + "/" + subResourceId;
			applyPreInterceptor(requestPath, bodyToMap, oldInstance, context);

			Object newInstance = bodyValidation(subResourceStructure, bodyToMap, oldInstance);
			subResourceStructure.getJpaRepository().saveAndFlush(newInstance);
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
		EntityStructure targetEntityStructure = JpaSmartQuerySupport.getStructure(columnStucture.getTargetEntity());
		ObjectMapper mapper = new ObjectMapper();
		Object readObject = null;
		try {
			readObject = CommonUtils.mapToObject(bodyToMap, targetEntityStructure.getEntityClass());
		} catch (IOException e) {
			e.printStackTrace();
			throw new PostFormatException(3286, "数据格式异常", "json解析错误");
		}
		BeanWrapperImpl wrapper = new BeanWrapperImpl(readObject);
		Object bodyId = wrapper.getPropertyValue("uuid");
		ColumnJoinType joinType = columnStucture.getJoinType();

		Object retUuid = null;

		if (joinType.equals(ColumnJoinType.ONE_TO_ONE)) {
			Object existSubResourceObject = ownerWrapper.getPropertyValue(subResource);
			if (existSubResourceObject != null) {
				throw new PostFormatException(3220, "资源已经存在", "资源已经存在");
			}
			Object bodyObject = withoutIdBodyValidation(targetEntityStructure, readObject);
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
			Object newInstance;
			try {
				newInstance = structure.getEntityClass().newInstance();
				BeanWrapperImpl newInstanceWrapperImpl = new BeanWrapperImpl(newInstance);
				newInstanceWrapperImpl.setPropertyValue(subResource, save);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else if (joinType.equals(ColumnJoinType.ONE_TO_MANY)) {

			Object bodyObject = withoutIdBodyValidation(targetEntityStructure, readObject);
			BeanWrapperImpl bodyObjectWrapper = new BeanWrapperImpl(bodyObject);
			bodyObjectWrapper.setPropertyValue(columnStucture.getMappedBy(), owner);
			Object save = targetEntityStructure.getJpaRepository().saveAndFlush(bodyObject);
			BeanWrapperImpl saveWrapper = new BeanWrapperImpl(save);
			retUuid = saveWrapper.getPropertyValue("uuid");

		} else if (joinType.equals(ColumnJoinType.MANY_TO_ONE)) {
			if (bodyId == null) {
				throw new PostFormatException(3240, "数据格式异常", "MANY_TO_ONE结构体只能关联，不允许创建");
			}
			Optional subResourceOptional = targetEntityStructure.getJpaRepository().findById(bodyId);
			if (!subResourceOptional.isPresent()) {
				throw new PostFormatException(3250, "资源不存在", bodyId);
			}

			ownerWrapper.setPropertyValue(subResource, subResourceOptional.get());
			Object save = structure.getJpaRepository().saveAndFlush(owner);
			retUuid = bodyId;

		} else if (joinType.equals(ColumnJoinType.MANY_TO_MANY)) {
			if (bodyId == null) {
				throw new PostFormatException(3260, "数据格式异常", "MANY_TO_MANY结构体只能关联，不允许创建");
			}
			Optional subResourceOptional = targetEntityStructure.getJpaRepository().findById(bodyId);
			if (!subResourceOptional.isPresent()) {
				throw new PostFormatException(3270, "资源不存在", bodyId);
			}
			Object newSubResource = subResourceOptional.get();
			if (columnStucture.getMappedBy() != null) {
				BeanWrapperImpl newSubResourceWrapper = new BeanWrapperImpl(newSubResource);
				Object propertyValue = newSubResourceWrapper.getPropertyValue(columnStucture.getMappedBy());
				((Set) propertyValue).add(owner);
				targetEntityStructure.getJpaRepository().saveAndFlush(newSubResource);
			} else {
				Object propertyValue = ownerWrapper.getPropertyValue(subResource);
				((Set) propertyValue).add(newSubResource);
				structure.getJpaRepository().saveAndFlush(owner);
			}
			retUuid = bodyId;
		}
		httpPostOkResponse.setUuid(retUuid.toString());
		return httpPostOkResponse;
	}

	private Object withoutIdBodyValidation(EntityStructure structure, Object body) {

		BeanWrapperImpl readWrapper = new BeanWrapperImpl(body);
		Object uuid = readWrapper.getPropertyValue("uuid");
		if (uuid != null) {
			throw new PostFormatException(3040, "数据格式异常", "创建对象不允许带uuid");
		}
		Map<String, String> validate = CommonUtils.validate(body);
		if (!validate.isEmpty()) {

			throw new PostFormatException(1000, "数据格式错误", validate);
		}
		return body;

	}

	private Object withoutIdBodyValidation(EntityStructure structure, Map<String, Object> bodyToMap) {

		try {
			Object readObject =  CommonUtils.mapToObject(bodyToMap, structure.getEntityClass());
			BeanWrapperImpl readWrapper = new BeanWrapperImpl(readObject);
			Object uuid = readWrapper.getPropertyValue("uuid");
			if (uuid != null) {
				throw new PostFormatException(3040, "数据格式异常", "创建对象不允许带uuid");
			}
			Map<String, String> validate = CommonUtils.validate(readObject);
			if (!validate.isEmpty()) {

				throw new PostFormatException(1000, "数据格式错误", validate);
			}
			return readObject;
		} catch (IOException e) {
			e.printStackTrace();
			throw new PostFormatException(3010, "数据格式异常", "json解析错误");
		}
	}

	private Object bodyValidation(EntityStructure structure, Map<String, Object> body) {

		try {
			Object readObject = CommonUtils.mapToObject(body, structure.getEntityClass());
			BeanWrapperImpl readWrapper = new BeanWrapperImpl(readObject);
			Map<String, String> validate = CommonUtils.validate(readObject);
			if (!validate.isEmpty()) {
				throw new PostFormatException(1000, "数据格式错误", validate);
			}
			return readObject;
		} catch (IOException e) {
			e.printStackTrace();
			throw new PostFormatException(3010, "数据格式异常", "json解析错误");
		}
	}

	private Object bodyValidation(EntityStructure structure, Map<String, Object> body, Object old) {
		try {
			Object readObject = CommonUtils.mapToObject(body, structure.getEntityClass());
			CommonUtils.copyPropertiesIgnoreNull(readObject, old);
			Map<String, String> validate = CommonUtils.validate(old);
			if (!validate.isEmpty()) {
				throw new PostFormatException(1000, "数据格式错误", validate);
			}
			return old;
		} catch (IOException e) {
			e.printStackTrace();
			throw new PostFormatException(3030, "数据格式异常", "json解析错误");
		}
	}

	private boolean applyPreInterceptor(String requestPath, Map<String, Object> body, Object oldInstance , Map<Object,Object> context) {
		if (this.interceptors != null && this.interceptors.size() != 0) {

			PathMatcher matcher = new AntPathMatcher();

			Collection<JpaRestfulPostInterceptor> values = this.interceptors.values();
			JpaRestfulPostInterceptor[] interceptors = values.toArray(new JpaRestfulPostInterceptor[values.size()]);
			Arrays.sort(interceptors, new InterceptorComparator());
			// 顺序执行拦截器的preHandle方法，如果返回false,则调用triggerAfterCompletion方法
			for (int i = 0; i < interceptors.length; i++) {
				JpaRestfulPostInterceptor interceptor = interceptors[i];

				String patternPath = interceptor.path();
				if (!matcher.match(patternPath, requestPath)) {
					continue;
				}
				if (!interceptor.preHandle(requestPath, body, oldInstance , context)) {
					throw new BusinessException(2000, "数据被拦截", "路径：" + interceptor.path());
				}
			}
		}
		return true;
	}

	private Object applyPostInterceptor(String requestPath, HTTPPostOkResponse httpPostOkResponse, Map<Object,Object> context) {
		if (this.interceptors != null && this.interceptors.size() != 0) {

			PathMatcher matcher = new AntPathMatcher();

			Collection<JpaRestfulPostInterceptor> values = this.interceptors.values();
			JpaRestfulPostInterceptor[] interceptors = values.toArray(new JpaRestfulPostInterceptor[values.size()]);
			Arrays.sort(interceptors, new InterceptorComparator());
			for (int i = interceptors.length - 1; i >= 0; i--) {
				JpaRestfulPostInterceptor interceptor = interceptors[i];
				String patternPath = interceptor.path();
				if (!matcher.match(patternPath, requestPath)) {
					continue;
				}
				httpPostOkResponse = interceptor.postHandle(requestPath, httpPostOkResponse, context);
			}
		}
		return httpPostOkResponse;

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.interceptors = applicationContext.getBeansOfType(JpaRestfulPostInterceptor.class);

	}

}
