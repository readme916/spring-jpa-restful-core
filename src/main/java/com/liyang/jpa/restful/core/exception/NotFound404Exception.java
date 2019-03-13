package com.liyang.jpa.restful.core.exception;



/**
 * 访问的实体不存在的异常
 *
 */
public class NotFound404Exception extends JpaRestfulException{

	public NotFound404Exception(Object detail) {
		super(404, "Not Found", "资源不存在", detail);
	}
	

}
