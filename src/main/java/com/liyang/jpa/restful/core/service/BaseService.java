package com.liyang.jpa.restful.core.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import com.liyang.jpa.restful.core.annotation.JpaRestfulResource;
import com.liyang.jpa.restful.core.event.EventManager;
import com.liyang.jpa.restful.core.exception.NotFound404Exception;
import com.liyang.jpa.restful.core.exception.ServerError500Exception;
import com.liyang.jpa.restful.core.utils.CommonUtils;
import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.ColumnJoinType;
import com.liyang.jpa.smart.query.db.structure.ColumnStucture;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;

public abstract class BaseService implements ApplicationContextAware {

	protected void checkSubsubResource(String resource, String subResource, String subsubResource,
			HashMap<String, String> params) {
		checkResource(resource, params);
		_checkSubResource(resource, subResource);
		String subResourceName = CommonUtils.subResourceName(resource, subResource);
		_checkSubResource(subResourceName, subsubResource);
	}

	protected void checkSubResource(String resource, String subResource, HashMap<String, String> params) {
		checkResource(resource, params);
		_checkSubResource(resource, subResource);
	}

	protected void checkResource(String resource, HashMap<String, String> params) {
		EntityStructure structure = SmartQuery.getStructure(resource);
		Class<?> cls = structure.getEntityClass();
		if (!cls.isAnnotationPresent(JpaRestfulResource.class)) {
			throw new NotFound404Exception("resource资源没有restful化的注解");
		}
//		if (params != null) {
//			String fields = params.getOrDefault("fields", "");
//			if (fields.indexOf(".") != -1) {
//				throw new ServerError500Exception("fields中不允许复合对象");
//			}
//		}
	}


	protected String reversePrefix(String resource, String subResource) {
		EntityStructure structure = SmartQuery.getStructure(resource);
		ColumnStucture columnStucture = structure.getObjectFields().get(subResource);
		Class<?> targetEntity = columnStucture.getTargetEntity();
		EntityStructure subStructure = SmartQuery.getStructure(targetEntity);
		String prefix = null;
		if (columnStucture.getMappedBy() != null) {
			prefix = columnStucture.getMappedBy();
		} else {
			Map<String, ColumnStucture> objectFields = subStructure.getObjectFields();
			Set<Entry<String, ColumnStucture>> entrySet = objectFields.entrySet();
			for (Entry<String, ColumnStucture> entry : entrySet) {
				if (entry.getValue().getMappedBy() != null) {
					if (entry.getValue().getTargetEntity() == structure.getEntityClass()) {
						if (entry.getValue().getMappedBy().equals(subResource)) {
							prefix = entry.getKey();
						}
					}

				}
			}
		}
		if (prefix == null) {
			throw new ServerError500Exception("格式错误");
		}
		return prefix;
	}

	private void _checkSubResource(String resource, String subResource) {
		EntityStructure structure = SmartQuery.getStructure(resource);
		ColumnStucture columnStucture = structure.getObjectFields().get(subResource);
		if (columnStucture == null) {
			throw new NotFound404Exception(subResource + "子资源不存在");
		}
//		if(columnStucture.getJoinType().equals(ColumnJoinType.MANY_TO_MANY)) {
//			throw new NotFound404Exception(subResource + "不允许MANY_TO_MANY查询方式");
//		}
//		if(columnStucture.getJoinType().equals(ColumnJoinType.MANY_TO_ONE)) {
//			throw new NotFound404Exception(subResource + "不允许MANY_TO_ONE查询方式");
//		}
	}
	
	@Transactional(readOnly = false)
	protected void publishEvent(String defaultEvent, Map<String, Object> bodyToMap, Object oldInstance) {
		if(bodyToMap!=null && bodyToMap.containsKey("event")) {
			defaultEvent = bodyToMap.get("event").toString();
		}
		EventManager entityManager = CommonUtils.getStructure(oldInstance.getClass()).getEventManager();
		if(entityManager!=null) {
			entityManager.dispatch(defaultEvent,bodyToMap,oldInstance);
		}
	}
}
