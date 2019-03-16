package com.liyang.jpa.restful.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liyang.jpa.restful.core.service.GetService;

/**
 * 用于业务中的各种抛出异常事件
 * 保留号码1000+给业务
 * @author liyang
 *
 */
public class Business503Exception extends JpaRestfulException{

	protected final static Logger logger = LoggerFactory.getLogger(Business503Exception.class);

	public Business503Exception(int status, String message, Object because) {
		super(status, "Service Unavailable" , message , because);
	}
	public Business503Exception() {
		super(1000, "Service Unavailable" , "" , "");
	}

}
