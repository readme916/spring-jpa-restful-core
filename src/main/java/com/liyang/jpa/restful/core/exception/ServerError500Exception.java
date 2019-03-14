package com.liyang.jpa.restful.core.exception;

/**
 * 内部异常
 * @author liyang
 *
 */
public class ServerError500Exception extends JpaRestfulException{

	public ServerError500Exception(Object because) {
		super(500, "Internal Server Error","内部服务错误", because);
		// TODO Auto-generated constructor stub
	}
	
	public ServerError500Exception() {
		super(500, "Internal Server Error","内部服务错误", "");
		// TODO Auto-generated constructor stub
	}
}
