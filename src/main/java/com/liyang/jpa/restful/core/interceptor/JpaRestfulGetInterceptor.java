package com.liyang.jpa.restful.core.interceptor;

import java.util.Map;

import com.liyang.jpa.restful.core.response.HTTPListResponse;

public interface JpaRestfulGetInterceptor extends JpaRestfulInterceptor {
	
	boolean preHandle(String requestPath, Map<String, String> params, Map<String,Object> context);

	Object postHandle(String requestPath, Object fetchList, Map<String,Object> context);

}
