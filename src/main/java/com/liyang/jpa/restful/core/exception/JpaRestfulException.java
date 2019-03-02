package com.liyang.jpa.restful.core.exception;

public interface JpaRestfulException {
	int getError();
	int getCode();
	String getMessage();
	Object getBecause();
}
