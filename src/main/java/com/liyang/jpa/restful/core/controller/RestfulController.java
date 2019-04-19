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
@RequestMapping("${spring.jpa.restful.path}")
@ConditionalOnProperty(name = "spring.jpa.restful.path")
public class RestfulController extends DefaultExceptionHandler{
	protected final static Logger logger = LoggerFactory.getLogger(RestfulController.class);
	@Autowired
	PostService postService;

	@Autowired
	GetService getService;
	
	@Autowired
	DeleteService deleteService;

	// 访问主资源，返回列表，带参数，可附加关联对象
	@RequestMapping(path = "{resource}", method = RequestMethod.GET)
	public Object get(@PathVariable String resource, @RequestParam(required = false) HashMap<String, String> params) {
		return getService.fetch(resource, params);
	}

	// 访问主资源，返回单体，带参数，可附加关联对象
	@RequestMapping(path = "{resource}/{resourceId}", method = RequestMethod.GET)
	public Object get(@PathVariable String resource, @PathVariable String resourceId,
			@RequestParam(required = false) HashMap<String, String> params) {
		return getService.fetch(resource, resourceId, params);
	}

	// 桥接访问子资源，返回列表，可附加子资源关联对象
	@RequestMapping(path = "{resource}/{resourceId}/{subResource}", method = RequestMethod.GET)
	public Object get(@PathVariable String resource, @PathVariable String resourceId, @PathVariable String subResource,
			@RequestParam(required = false) HashMap<String, String> params) {
		return getService.fetch(resource, resourceId, subResource, params);
	}

	// 桥接访问子资源，返回单体，可附加关联对象
	@RequestMapping(path = "{resource}/{resourceId}/{subResource}/{subResourceId}", method = RequestMethod.GET)
	public Object get(@PathVariable String resource, @PathVariable String resourceId, @PathVariable String subResource,
			@PathVariable String subResourceId, @RequestParam(required = false) HashMap<String, String> params) {
		return getService.fetch(resource, resourceId, subResource, subResourceId, params);
	}

	// 桥接访问孙子资源，返回列表，可附加关联对象
	@RequestMapping(path = "{resource}/{resourceId}/{subResource}/{subResourceId}/{subsubResource}", method = RequestMethod.GET)
	public Object get(@PathVariable String resource, @PathVariable String resourceId, @PathVariable String subResource,
			@PathVariable String subResourceId, @PathVariable String subsubResource,
			@RequestParam(required = false) HashMap<String, String> params) {
		return getService.fetch(resource, resourceId, subResource, subResourceId, subsubResource, params);
	}

	// POST

	// 创建资源
	@RequestMapping(path = "{resource}", method = RequestMethod.POST)
	public Object create(@PathVariable String resource, @RequestBody(required = true) String body) {
		return postService.create(resource, body);
	}

	// 更新资源
	@RequestMapping(path = "{resource}/{resourceId}", method = RequestMethod.POST)
	public Object update(@PathVariable String resource, @PathVariable String resourceId,
			@RequestBody(required = true) String body) {
		return postService.update(resource, resourceId, body);
	}

	// 桥接创建资源，子资源可以创建，非子资源只能关联
	@RequestMapping(path = "{resource}/{resourceId}/{subResource}", method = RequestMethod.POST)
	public Object sub_create(@PathVariable String resource, @PathVariable String resourceId,
			@PathVariable String subResource, @RequestBody(required = true) String body) {
		return postService.create(resource, resourceId, subResource, body);
	}

	// 桥接更新资源，必须为子资源
	@RequestMapping(path = "{resource}/{resourceId}/{subResource}/{subResourceId}", method = RequestMethod.POST)
	public Object sub_update(@PathVariable String resource, @PathVariable String resourceId,
			@PathVariable String subResource, @PathVariable String subResourceId,
			@RequestBody(required = true) String body) {
		return postService.update(resource, resourceId, subResource, subResourceId, body);
	}

	// 桥接创建孙子资源，父和子关系必须，子和孙子关系时可以创建，非孙子关系只能关联
	@RequestMapping(path = "{resource}/{resourceId}/{subResource}/{subResourceId}/{subsubResourceProperty}", method = RequestMethod.POST)
	public Object sub_sub_create(@PathVariable String resource, @PathVariable String resourceId,
			@PathVariable String subResource, @PathVariable String subResourceId,
			@PathVariable String subsubResourceProperty, @RequestBody(required = true) String body) {
		return postService.create(resource, resourceId, subResource, subResourceId, subsubResourceProperty, body);
	}
	
	
	//Delete
	// 删除资源
	@RequestMapping(path = "{resource}/{resourceId}", method = RequestMethod.DELETE)
	public Object delete(@PathVariable String resource, @PathVariable String resourceId) {
		return deleteService.delete(resource, resourceId);
	}
	
	// 桥接删除资源，必须为子资源
	@RequestMapping(path = "{resource}/{resourceId}/{subResource}/{subResourceId}", method = RequestMethod.DELETE)
	public Object sub_update(@PathVariable String resource, @PathVariable String resourceId,
			@PathVariable String subResource, @PathVariable String subResourceId) {
		return deleteService.delete(resource, resourceId, subResource, subResourceId);
	}
}