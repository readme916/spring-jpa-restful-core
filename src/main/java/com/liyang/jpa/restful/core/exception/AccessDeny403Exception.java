package com.liyang.jpa.restful.core.exception;

import org.springframework.http.HttpStatus;

/**
 * 用于没有权限访问异常
 * @author liyang
 *
 */
public class AccessDeny403Exception extends JpaRestfulException{
	public AccessDeny403Exception(Object detail) {
		super(403, "Forbidden", "无访问权限", detail);
	}
	public AccessDeny403Exception() {
		super(403, "Forbidden", "无访问权限", "");
	}

}
