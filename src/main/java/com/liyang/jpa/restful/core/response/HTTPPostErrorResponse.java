package com.liyang.jpa.restful.core.response;

import java.util.Date;

import com.liyang.jpa.restful.core.exception.JpaRestfulException;

public class HTTPPostErrorResponse {

	private int error=1;
	private int code=1000;
	private String message="";
	private Object because=null;
	
	public HTTPPostErrorResponse(JpaRestfulException exception){
		this.error = exception.getError();
		this.code = exception.getCode();
		this.message = exception.getMessage();
		this.because = exception.getBecause();
	}

	public int getError() {
		return error;
	}

	public void setError(int error) {
		this.error = error;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getBecause() {
		return because;
	}

	public void setBecause(Object because) {
		this.because = because;
	}
	
		
}
