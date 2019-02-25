package com.liyang.jpa.restful.exception;



/**
 * 用于业务中的各种抛出异常事件
 * 保留号码2001-3000给业务
 * 拦截器的拦截事件code定义为2000
 * @author liyang
 *
 */
public class BusinessException extends RuntimeException implements JpaRestfulException{
	private int error=1;
	private int code;
	private String message;
	private Object because;

	public BusinessException(int code, String message, Object because) {
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
