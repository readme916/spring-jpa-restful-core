package com.liyang.jpa.restful.core.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;

import com.liyang.jpa.restful.core.annotation.AllowCondition;
import com.liyang.jpa.restful.core.annotation.AllowFields;
import com.liyang.jpa.restful.core.annotation.ForbidFields;
import com.liyang.jpa.restful.core.event.RestfulEvent;
import com.liyang.jpa.restful.core.exception.Business503Exception;
import com.liyang.jpa.restful.core.exception.JpaRestfulException;
import com.liyang.jpa.restful.core.exception.ServerError500Exception;
import com.liyang.jpa.restful.core.exception.Validator422Exception;
import com.liyang.jpa.restful.core.utils.SpelContext;

public abstract class RestfulEventListener<T> implements ApplicationListener<RestfulEvent> {

	@Override
	public void onApplicationEvent(RestfulEvent event) {
		Class<?> entityClass = event.getEntityStructure().getEntityClass();
		Class<?> resolve = ResolvableType.forClass(this.getClass()).as(RestfulEventListener.class).getGeneric(0)
				.resolve();
		if (resolve.equals(entityClass)) {
			String upperCase = event.getEvent().substring(0, 1).toUpperCase();
			String on = "on" + upperCase + event.getEvent().substring(1);
			Method findMethod = ReflectionUtils.findMethod(this.getClass(), on, entityClass);
			if (findMethod != null) {

				AllowFields allowAnnotation = findMethod.getAnnotation(AllowFields.class);
				if (allowAnnotation != null) {
					allowField(event.getSource(), allowAnnotation.value());
				} else {
					ForbidFields forbidAnnotation = findMethod.getAnnotation(ForbidFields.class);
					if (forbidAnnotation != null) {
						forbidField(event.getSource(), forbidAnnotation.value());
					}
				}

				AllowCondition allowCondition = findMethod.getAnnotation(AllowCondition.class);
				if (allowCondition != null) {
					allowCondition(event.getSource(), allowCondition.value());
				}
				
				
				try {
					findMethod.invoke(this, event.getSource());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					Throwable cause = e.getCause();
					if (cause instanceof JpaRestfulException) {
						JpaRestfulException ex = (JpaRestfulException) cause;
						throw ex;
					} else {
						e.printStackTrace();
					}
				}
			}else {
				throw new ServerError500Exception("不支持"+event.getEvent()+"事件");
			}
		}
	}

	private void allowCondition(Object source, String condition) {
		SpelContext spelContext = new SpelContext(source);
		ExpressionParser parser = new SpelExpressionParser();
		Boolean ret = parser.parseExpression(condition, new TemplateParserContext())
				.getValue(spelContext, Boolean.class);
		if(!ret) {
			throw new Business503Exception(1321,"非法操作",condition);
		}
		
	}

	private void forbidField(Object obj, String[] filterField) {
		final BeanWrapper src = new BeanWrapperImpl(obj);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
		Set<String> filter = new HashSet<String>();
		for (String str : filterField) {
			filter.add(str);
		}
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (srcValue!=null && filter.contains(pd.getName())) {
				throw new Validator422Exception("非法字段"+ pd.getName());
			} else {
				continue;
			}
		}
	}

	private void allowField(Object obj, String[] allowField) {
		final BeanWrapper src = new BeanWrapperImpl(obj);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
		Set<String> allow = new HashSet<String>();
		for (String str : allowField) {
			allow.add(str);
		}
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (allow.contains(pd.getName()) || pd.getName().equals("class")||pd.getName().equals("uuid") || srcValue==null) {
				continue;
			} else {
				src.setPropertyValue(pd.getName(), null);
				throw new Validator422Exception("非法字段"+ pd.getName());
			}
		}
	}
}
