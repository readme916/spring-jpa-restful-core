package com.liyang.jpa.restful.core.exception;

/**
 * 使用jparestful的post方法时候的各种输入验证错误
 * 号码段：3000-5000
 * @author liyang
 *
 */
public class PostFormatException extends RuntimeException implements JpaRestfulException{
	private int error=1;
	private int code;
	private String message;
	private Object because;
	public PostFormatException(int code, String message, Object because) {
		super();
		this.code = code;
		this.message = message;
		this.because = because;
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
