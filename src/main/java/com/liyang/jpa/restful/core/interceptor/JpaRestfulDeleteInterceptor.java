package com.liyang.jpa.restful.core.interceptor;

import java.util.Map;

import com.liyang.jpa.restful.core.response.HTTPListResponse;
import com.liyang.jpa.restful.core.response.HTTPPostOkResponse;

public interface JpaRestfulDeleteInterceptor extends JpaRestfulInterceptor  {

	boolean preHandle(String requestPath, Object oldInstance, Map<Object,Object> context);

	HTTPPostOkResponse postHandle(String requestPath, HTTPPostOkResponse httpPostOkResponse, Map<Object,Object> context);


}
