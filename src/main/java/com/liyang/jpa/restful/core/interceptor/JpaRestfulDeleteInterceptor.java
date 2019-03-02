package com.liyang.jpa.restful.core.interceptor;

import java.util.Map;

import com.liyang.jpa.restful.core.response.HTTPListResponse;

public interface JpaRestfulDeleteInterceptor extends JpaRestfulInterceptor  {

	boolean preHandle(String requestPath, Object oldInstance, Map<Object,Object> context);

	Object postHandle(String requestPath, Object httpPostOkResponse, Map<Object,Object> context);


}
