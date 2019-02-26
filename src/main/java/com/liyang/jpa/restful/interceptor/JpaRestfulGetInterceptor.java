package com.liyang.jpa.restful.interceptor;

import java.util.Map;

import com.liyang.jpa.restful.response.HTTPListResponse;

public interface JpaRestfulGetInterceptor extends JpaRestfulInterceptor {
	
	boolean preHandle(String requestPath, Map<String, String> params);

	Object postHandle(String requestPath, Object fetchList);

}
