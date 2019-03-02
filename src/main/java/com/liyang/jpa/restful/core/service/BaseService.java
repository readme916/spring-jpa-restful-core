package com.liyang.jpa.restful.core.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.context.ApplicationContextAware;

import com.liyang.jpa.mysql.config.JpaSmartQuerySupport;
import com.liyang.jpa.mysql.db.structure.ColumnJoinType;
import com.liyang.jpa.mysql.db.structure.ColumnStucture;
import com.liyang.jpa.mysql.db.structure.EntityStructure;
import com.liyang.jpa.mysql.exception.GetFormatException;
import com.liyang.jpa.restful.core.annotation.JpaRestfulResource;
import com.liyang.jpa.restful.core.utils.CommonUtils;

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
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		Class<?> cls = structure.getEntityClass();
		if (!cls.isAnnotationPresent(JpaRestfulResource.class)) {
			throw new GetFormatException(7971, "查询异常", "资源没有restful化的注解");
		}
		if (params != null) {
			String fields = params.getOrDefault("fields", "");
			if (fields.indexOf(".") != -1) {
				throw new GetFormatException(7888, "查询异常", "fields中不允许复合对象");
			}
		}
	}


	protected String reversePrefix(String resource, String subResource) {
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		ColumnStucture columnStucture = structure.getObjectFields().get(subResource);
		Class<?> targetEntity = columnStucture.getTargetEntity();
		EntityStructure subStructure = JpaSmartQuerySupport.getStructure(targetEntity);
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
			throw new GetFormatException(7212, "查询异常", "格式错误");
		}
		return prefix;
	}

	private void _checkSubResource(String resource, String subResource) {
		EntityStructure structure = JpaSmartQuerySupport.getStructure(resource);
		ColumnStucture columnStucture = structure.getObjectFields().get(subResource);
		if (columnStucture == null || columnStucture.getJoinType().equals(ColumnJoinType.MANY_TO_MANY)
				|| columnStucture.getJoinType().equals(ColumnJoinType.MANY_TO_ONE)) {
			throw new GetFormatException(7818, "查询异常", subResource + "非子资源");
		}
	}
}
