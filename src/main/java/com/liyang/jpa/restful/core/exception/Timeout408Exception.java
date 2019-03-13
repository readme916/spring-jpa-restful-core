package com.liyang.jpa.restful.core.exception;



/**
 * 访问的实体不存在的异常
 *
 */
public class Timeout408Exception extends JpaRestfulException{
	
	public Timeout408Exception(Object because) {
		super(408, "Request Timeout" , "连接超时", because);
		// TODO Auto-generated constructor stub
	}
	

}
