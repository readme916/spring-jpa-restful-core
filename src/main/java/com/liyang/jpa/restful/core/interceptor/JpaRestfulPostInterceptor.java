package com.liyang.jpa.restful.core.interceptor;

import java.util.Map;

import com.liyang.jpa.restful.core.response.HTTPPostOkResponse;

public interface JpaRestfulPostInterceptor extends JpaRestfulInterceptor {

	public boolean preHandle(String requestPath, Map<String, Object> requestBody, Object onwerInstance, Map<String,Object> context);

	public HTTPPostOkResponse postHandle(String requestPath, HTTPPostOkResponse httpPostOkResponse, Map<String,Object> context);


}
