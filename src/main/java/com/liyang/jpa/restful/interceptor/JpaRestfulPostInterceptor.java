package com.liyang.jpa.restful.interceptor;

import java.util.Map;

import com.liyang.jpa.restful.response.HTTPListResponse;

public interface JpaRestfulPostInterceptor extends JpaRestfulInterceptor {

	boolean preHandle(String requestPath, String requestBody, Object oldInstance);

	Object postHandle(String requestPath, Object httpPostOkResponse);


}
