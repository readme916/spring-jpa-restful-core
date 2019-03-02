package com.liyang.jpa.restful.core.interceptor;

public interface JpaRestfulInterceptor {

	String name();

	String description();

	String path();

	int order();

}