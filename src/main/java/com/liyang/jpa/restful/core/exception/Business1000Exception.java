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
public class Business1000Exception extends JpaRestfulException{

	protected final static Logger logger = LoggerFactory.getLogger(Business1000Exception.class);

	public Business1000Exception(int status, String message, Object because) {
		super(status, "Business Error" , message , because);
		if(status<1000) {
			logger.error("业务异常号应该大于1000");
		}
		// TODO Auto-generated constructor stub
	}
	

}
