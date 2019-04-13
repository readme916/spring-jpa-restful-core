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

import com.liyang.jpa.restful.core.service.CheckService;
import com.liyang.jpa.restful.core.service.DeleteService;
import com.liyang.jpa.restful.core.service.EurekaService;
import com.liyang.jpa.restful.core.service.GetService;
import com.liyang.jpa.restful.core.service.PostService;
import com.liyang.jpa.restful.core.utils.EntityStructureEx;

@RestController
@ConditionalOnProperty(name = "eureka.client.serviceUrl.defaultZone")
public class StructureController extends DefaultExceptionHandler{
	protected final static Logger logger = LoggerFactory.getLogger(StructureController.class);

	@Autowired
	EurekaService eurekaService;
	
	@RequestMapping(path="/structure",method=RequestMethod.GET)
	public Object structure(@RequestParam(required = false) HashMap<String, String> params) {
		HashMap<String, EntityStructureEx> structure = CheckService.nameToStructure;
		return structure;
	}
}