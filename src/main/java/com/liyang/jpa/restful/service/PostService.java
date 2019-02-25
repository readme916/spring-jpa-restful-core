package com.liyang.jpa.restful.service;

import java.io.IOException;
import java.util.Collection;
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
import com.liyang.jpa.restful.exception.BusinessException;
import com.liyang.jpa.restful.exception.PostFormatException;
import com.liyang.jpa.restful.interceptor.JpaRestfulPostInterceptor;
import com.liyang.jpa.restful.response.HTTPPostOkResponse;
import com.liyang.jpa.restful.utils.CommonUtils;

@Service
public class PostService extends BaseService {
	private Map<String, JpaRestfulPostInterceptor> interceptors;
	protected final static Logger logger = LoggerFactory.getLogger(PostService.class);

	@Transactional(readOnly = false)
	public Object create(String resource, String body) {
		checkResource(resource, null);
		String requestPath = "/" + resource;
		applyPreInterceptor(requestPath, body, null);
		
		Object readObject = withoutIdBodyValidation(resource, body);
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		Object save = structure.getJpaRepository().save(readObject);
		BeanWrapperImpl saveImpl = new BeanWrapperImpl(save);
		Object savedUUID = saveImpl.getPropertyValue("uuid");
		HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
		httpPostOkResponse.setUuid(savedUUID.toString());
		return applyPostInterceptor(requestPath, save);
	}



	@Transactional(readOnly = false)
	public Object create(String resource, String resourceId, String subResource, String body) {
		checkResource(resource, null);
		
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		Object owner;
		Optional ownerOptional = structure.getJpaRepository().findById(resourceId);
		if (!ownerOptional.isPresent()) {
			throw new PostFormatException(3100, "数据不存在", resourceId);
		} else {
			owner = ownerOptional.get();
		}
		String requestPath = "/" + resource + "/" + resourceId + "/" + subResource;
		applyPreInterceptor(requestPath, body, null);

		Object uuid = subResourceCreate(structure, owner, subResource, body);
		HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
		httpPostOkResponse.setUuid(uuid.toString());
		return applyPostInterceptor(requestPath, httpPostOkResponse);
	}
	@Transactional(readOnly = false)
	public Object create(String resource, String resourceId, String subResource, String subResourceId,
			String subsubResource, String body) {
		checkSubResource(resource,subResource, null);

		long fetchCount = SmartQuery.fetchCount(resource,
				"uuid=" + resourceId + "&" + subResource + ".uuid=" + subResourceId);
		if (fetchCount == 0) {
			throw new PostFormatException(3530, "数据不存在", "");
		}
		String requestPath = "/" + resource + "/" + resourceId + "/" + subResource+"/"+subResourceId+"/"+subsubResource;
		applyPreInterceptor(requestPath, body, null);
		
		String subResourceName = subResourceName(resource, subResource);
		EntityStructure subStructure = JpaSmartQuerySupport.getStructure(subResourceName);
		Optional ownerOptional = subStructure.getJpaRepository().findById(subResourceId);
		Object owner = ownerOptional.get();
		Object uuid = subResourceCreate(subStructure, owner, subsubResource, body);

		HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
		httpPostOkResponse.setUuid(uuid.toString());
		return applyPostInterceptor(requestPath, httpPostOkResponse);
	}
	

	@Transactional(readOnly = false)
	public Object update(String resource, String resourceId, String body) {
		checkResource(resource, null);
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		Object oldInstance;
		Optional oldInstanceOptional = structure.getJpaRepository().findById(resourceId);
		if (!oldInstanceOptional.isPresent()) {
			throw new PostFormatException(3100, "数据不存在", "");
		} else {
			oldInstance = oldInstanceOptional.get();
		}
		String requestPath = "/" + resource + "/" + resourceId;
		applyPreInterceptor(requestPath, body, oldInstance);

		Object newInstance = bodyValidation(resource, body, oldInstance);

		structure.getJpaRepository().save(newInstance);
		HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
		httpPostOkResponse.setUuid(resourceId);
		return applyPostInterceptor(requestPath, httpPostOkResponse);
	}
	
	@Transactional(readOnly = false)
	public Object update(String resource, String resourceId, String subResource, String subResourceId, String body) {
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		checkSubResource(resource, subResource, null);
		
		long fetchCount = SmartQuery.fetchCount(resource,
				"uuid=" + resourceId + "&" + subResource + ".uuid=" + subResourceId);
		if (fetchCount == 0) {
			throw new PostFormatException(3330, "数据不存在", "");
		} else {
			EntityStructure subResourceStructure = JpaSmartQuerySupport.getStructure(subResourceName(resource, subResource));
			Optional oldInstanceOptional = subResourceStructure.getJpaRepository().findById(subResourceId);
			Object oldInstance = oldInstanceOptional.get();
			String requestPath = "/" + resource + "/" + resourceId + "/" + subResource + "/" + subResourceId;
			applyPreInterceptor(requestPath, body, oldInstance);

			Object newInstance = bodyValidation(subResourceName(resource, subResource), body, oldInstance);
			subResourceStructure.getJpaRepository().save(newInstance);
			HTTPPostOkResponse httpPostOkResponse = new HTTPPostOkResponse();
			httpPostOkResponse.setUuid(subResourceId);
			return applyPostInterceptor(requestPath, httpPostOkResponse);
		}
	}

	private Object subResourceCreate(EntityStructure structure, Object owner, String subResource, String body) {
		BeanWrapperImpl ownerWrapper = new BeanWrapperImpl(owner);
		ColumnStucture columnStucture = structure.getObjectFields().get(subResource);
		EntityStructure targetEntityStructure = JpaSmartQuerySupport.getStructure(columnStucture.getTargetEntity());
		ObjectMapper mapper = new ObjectMapper();
		Object readObject = null;
		try {
			readObject = mapper.readValue(body, targetEntityStructure.getEntityClass());
		} catch (IOException e) {
			e.printStackTrace();
			throw new PostFormatException(3280, "数据格式异常", "json解析错误");
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
			Object bodyObject = withoutIdBodyValidation(targetEntityStructure.getName(), body);
			if (columnStucture.getMappedBy() != null) {
				BeanWrapperImpl bodyObjectWrapper = new BeanWrapperImpl(bodyObject);
				bodyObjectWrapper.setPropertyValue(columnStucture.getMappedBy(), owner);
				Object save = targetEntityStructure.getJpaRepository().save(bodyObject);
				BeanWrapperImpl saveWrapper = new BeanWrapperImpl(save);
				retUuid = saveWrapper.getPropertyValue("uuid");

			} else {
				Object subResourceObject = targetEntityStructure.getJpaRepository().save(bodyObject);
				BeanWrapperImpl saveWrapper = new BeanWrapperImpl(subResourceObject);
				retUuid = saveWrapper.getPropertyValue("uuid");
				ownerWrapper.setPropertyValue(subResource, subResourceObject);
				structure.getJpaRepository().save(owner);
			}
		} else if (joinType.equals(ColumnJoinType.ONE_TO_MANY)) {
		
			Object bodyObject = withoutIdBodyValidation(targetEntityStructure.getName(), body);
			BeanWrapperImpl bodyObjectWrapper = new BeanWrapperImpl(bodyObject);
			bodyObjectWrapper.setPropertyValue(columnStucture.getMappedBy(), owner);
			Object save = targetEntityStructure.getJpaRepository().save(bodyObject);
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
			Object save = structure.getJpaRepository().save(owner);
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
				targetEntityStructure.getJpaRepository().save(newSubResource);
			} else {
				Object propertyValue = ownerWrapper.getPropertyValue(subResource);
				((Set) propertyValue).add(newSubResource);
				structure.getJpaRepository().save(owner);
			}
			retUuid = bodyId;
		}
		return retUuid;
	}

	
	private Object withoutIdBodyValidation(String resource, String body) {
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		try {
			ObjectMapper mapper = new ObjectMapper();
			Object readObject = mapper.readValue(body, structure.getEntityClass());
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

	private Object bodyValidation(String resource, String body, Object old) {
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		try {
			ObjectMapper mapper = new ObjectMapper();
			Object readObject = mapper.readValue(body, structure.getEntityClass());
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



	private boolean applyPreInterceptor(String requestPath, String body, Object oldInstance) {
		if (this.interceptors != null && this.interceptors.size() != 0) {

			PathMatcher matcher = new AntPathMatcher();

			Collection<JpaRestfulPostInterceptor> values = this.interceptors.values();
			JpaRestfulPostInterceptor[] interceptors = values.toArray(new JpaRestfulPostInterceptor[values.size()]);
			// 顺序执行拦截器的preHandle方法，如果返回false,则调用triggerAfterCompletion方法
			for (int i = 0; i < interceptors.length; i++) {
				JpaRestfulPostInterceptor interceptor = interceptors[i];

				String patternPath = interceptor.path();
				if (!matcher.match(patternPath, requestPath)) {
					continue;
				}
				if (!interceptor.preHandle(requestPath, body, oldInstance)) {
					throw new BusinessException(2000, "数据被拦截", "路径：" + interceptor.path());
				}
			}
		}
		return true;
	}

	private Object applyPostInterceptor(String requestPath, Object httpPostOkResponse) {
		if (this.interceptors != null && this.interceptors.size() != 0) {

			PathMatcher matcher = new AntPathMatcher();

			Collection<JpaRestfulPostInterceptor> values = this.interceptors.values();
			JpaRestfulPostInterceptor[] interceptors = values.toArray(new JpaRestfulPostInterceptor[values.size()]);
			for (int i = interceptors.length - 1; i >= 0; i--) {
				JpaRestfulPostInterceptor interceptor = interceptors[i];
				String patternPath = interceptor.path();
				if (!matcher.match(patternPath, requestPath)) {
					continue;
				}
				httpPostOkResponse = interceptor.postHandle(requestPath, httpPostOkResponse);
			}
		}
		return httpPostOkResponse;

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.interceptors = applicationContext.getBeansOfType(JpaRestfulPostInterceptor.class);

	}



}
