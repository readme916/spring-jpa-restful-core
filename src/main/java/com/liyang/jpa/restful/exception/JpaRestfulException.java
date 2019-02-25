package com.liyang.jpa.restful.exception;

public interface JpaRestfulException {
	int getError();
	int getCode();
	String getMessage();
	Object getBecause();
}
