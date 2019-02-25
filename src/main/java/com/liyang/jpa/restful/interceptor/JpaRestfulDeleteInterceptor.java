package com.liyang.jpa.restful.interceptor;

import java.util.Map;

import com.liyang.jpa.restful.response.HTTPListResponse;

public interface JpaRestfulDeleteInterceptor {

	String name();
	
	String description();
	
	String path();

	boolean preHandle(String requestPath, Object oldInstance);

	Object postHandle(String requestPath, Object httpPostOkResponse);


}
