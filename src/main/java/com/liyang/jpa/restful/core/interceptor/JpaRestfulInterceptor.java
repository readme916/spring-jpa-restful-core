package com.liyang.jpa.restful.core.interceptor;

public interface JpaRestfulInterceptor {

	public String name();

	public String description();

	public String[] path();

	public int order();

}