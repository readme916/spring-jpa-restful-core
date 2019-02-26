package com.liyang.jpa.restful.interceptor;

public interface JpaRestfulInterceptor {

	String name();

	String description();

	String path();

	int order();

}