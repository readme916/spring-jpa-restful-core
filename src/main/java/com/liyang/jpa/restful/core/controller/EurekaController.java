package com.liyang.jpa.restful.core.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.restful.core.service.DeleteService;
import com.liyang.jpa.restful.core.service.EurekaService;
import com.liyang.jpa.restful.core.service.GetService;
import com.liyang.jpa.restful.core.service.PostService;

@RestController
@ConditionalOnProperty(name = "eureka.client.serviceUrl.defaultZone")
public class EurekaController extends DefaultExceptionHandler{
	protected final static Logger logger = LoggerFactory.getLogger(EurekaController.class);

	@Autowired
	EurekaService eurekaService;
	
	
	@RequestMapping(path = "/eureka/status", method = RequestMethod.GET)
	public Object getStatus() {
		return eurekaService.getStatus();
	}
	
	@RequestMapping(path = "/eureka/online", method = RequestMethod.POST)
	public Object online() {
		return eurekaService.online();
	}
	@RequestMapping(path = "/eureka/offline", method = RequestMethod.POST)
	public Object offline() {
		return eurekaService.offline();
	}
}