package com.liyang.jpa.restful.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.deser.Deserializers.Base;
import com.liyang.jpa.mysql.db.SmartQuery;
import com.liyang.jpa.mysql.exception.GetFormatException;
import com.liyang.jpa.restful.annotation.JpaRestfulResource;
import com.liyang.jpa.restful.config.JpaRestfulSupport;

import com.liyang.jpa.restful.exception.BusinessException;
import com.liyang.jpa.restful.exception.PostFormatException;
import com.liyang.jpa.restful.interceptor.JpaRestfulGetInterceptor;
import com.liyang.jpa.restful.utils.CommonUtils;
import com.liyang.jpa.restful.utils.InterceptorComparator;

@Service
public class GetService extends BaseService {

	private Map<String, JpaRestfulGetInterceptor> interceptors;
	protected final static Logger logger = LoggerFactory.getLogger(GetService.class);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.interceptors = applicationContext.getBeansOfType(JpaRestfulGetInterceptor.class);

	}

	public Object fetch(String resource, HashMap<String, String> params) {
		checkResource(resource,params);
		String requestPath = "/"+resource;
		applyPreInterceptor(requestPath, params);
		Object fetchList = SmartQuery.fetchList(resource, params);
		return applyPostInterceptor(requestPath, fetchList);
	}

	public Object fetch(String resource, String resourceId, HashMap<String, String> params) {
		checkResource(resource,params);
		String requestPath = "/"+resource+"/" + resourceId;
		applyPreInterceptor(requestPath, params);
		
		params.put("uuid", resourceId);
		params.putIfAbsent("fields", "*");
		
		Object fetchOne = SmartQuery.fetchOne(resource, params);
		return applyPostInterceptor(requestPath,fetchOne);
	}

	public Object fetch(String resource, String resourceId, String subResource, HashMap<String, String> params) {
		checkSubResource(resource,subResource,params);
		String requestPath = "/"+resource+"/" + resourceId + "/" + subResource;
		applyPreInterceptor(requestPath, params);
		
		String reversePrefix = reversePrefix(resource,subResource);
		String subResourceName = subResourceName(resource,subResource);
		
		params.put(reversePrefix+".uuid", resourceId);
		params.putIfAbsent("fields", "*");
		
		Object fetchList = SmartQuery.fetchList(subResourceName, params);
		return applyPostInterceptor(requestPath, fetchList);
	}


	public Object fetch(String resource, String resourceId, String subResource, String subResourceId,  HashMap<String, String> params) {
		checkSubResource(resource,subResource,params);
		String requestPath = "/"+resource+"/" + resourceId + "/" + subResource + "/" + subResourceId;
		applyPreInterceptor(requestPath, params);
		
		String reversePrefix = reversePrefix(resource,subResource);
		String subResourceName = subResourceName(resource,subResource);
		
		params.put("uuid", subResourceId);
		params.put(reversePrefix+".uuid", resourceId);
		params.putIfAbsent("fields", "*");
		
		Object fetchOne = SmartQuery.fetchOne(subResourceName, params);
		return applyPostInterceptor(requestPath, fetchOne);
	}

	public Object fetch(String resource, String resourceId, String subResource, String subResourceId,
			String subsubResource, HashMap<String, String> params) {
		checkSubsubResource(resource, subResource, subsubResource, params);
		String requestPath = "/"+resource+"/" + resourceId + "/" + subResource + "/" + subResourceId + "/" + subsubResource;
		applyPreInterceptor(requestPath, params);
		
		String subResourceName = subResourceName(resource,subResource);
		String subsubResourceName = subResourceName(subResourceName,subsubResource);
		String reversePrefix = reversePrefix(subResourceName,subsubResource);
		params.put(reversePrefix+".uuid", subResourceId);
		params.putIfAbsent("fields", "*");

		long fetchCount = SmartQuery.fetchCount(resource,
				"uuid=" + resourceId + "&" + subResource + ".uuid=" + subResourceId);
		if (fetchCount == 0) {
			throw new GetFormatException(6212,"查询异常","数据不存在");
		}		
		Object fetchList = SmartQuery.fetchList(subsubResourceName, params);
		return applyPostInterceptor(requestPath,fetchList);
	}


	private Object applyPostInterceptor(String requestPath, Object fetchList) {
		if (this.interceptors != null && this.interceptors.size() != 0) {

			PathMatcher matcher = new AntPathMatcher();

			Collection<JpaRestfulGetInterceptor> values = this.interceptors.values();
			JpaRestfulGetInterceptor[] interceptors = values.toArray(new JpaRestfulGetInterceptor[values.size()]);
			Arrays.sort(interceptors, new InterceptorComparator());
			
			for (int i = interceptors.length - 1; i >= 0; i--) {
				JpaRestfulGetInterceptor interceptor = interceptors[i];
				String patternPath = interceptor.path();
				if (!matcher.match(patternPath, requestPath)) {
					continue;
				}
				fetchList = interceptor.postHandle(requestPath , fetchList);
			}
		}
		return fetchList;

	}
	

	
	private boolean applyPreInterceptor(String requestPath ,HashMap<String, String> params) {
		if (this.interceptors != null && this.interceptors.size() != 0) {

			PathMatcher matcher = new AntPathMatcher();

			Collection<JpaRestfulGetInterceptor> values = this.interceptors.values();
			JpaRestfulGetInterceptor[] interceptors = values.toArray(new JpaRestfulGetInterceptor[values.size()]);
			Arrays.sort(interceptors, new InterceptorComparator());
			
			// 顺序执行拦截器的preHandle方法，如果返回false,则调用triggerAfterCompletion方法
			for (int i = 0; i < interceptors.length; i++) {
				JpaRestfulGetInterceptor interceptor = interceptors[i];

				String patternPath = interceptor.path();
				if (!matcher.match(patternPath, requestPath)) {
					continue;
				}
				if (!interceptor.preHandle(requestPath, params)) {
					throw new BusinessException(2000, "数据被拦截", "路径："+interceptor.path());
				}
			}
		}
		return true;
	}
}
