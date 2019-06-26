package com.liyang.jpa.restful.core.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ReflectionUtils;

import com.liyang.jpa.restful.core.annotation.event.AllowCondition;
import com.liyang.jpa.restful.core.annotation.event.AllowFields;
import com.liyang.jpa.restful.core.annotation.event.ForbidFields;
import com.liyang.jpa.restful.core.exception.AccessDeny403Exception;
import com.liyang.jpa.restful.core.exception.Business503Exception;
import com.liyang.jpa.restful.core.exception.JpaRestfulException;
import com.liyang.jpa.restful.core.exception.ServerError500Exception;
import com.liyang.jpa.restful.core.exception.Validator422Exception;
import com.liyang.jpa.restful.core.utils.EntityStructureEx;
import com.liyang.jpa.restful.core.utils.SpelContext;
import com.liyang.jpa.smart.query.db.SmartQuery;

public abstract class EventManager<T>{

	public void dispatch(String name,  Map<String, Object> bodyToMap, Object oldInstance) {
		
		Class<?> resolve = ResolvableType.forClass(this.getClass()).as(EventManager.class).getGeneric(0)
				.resolve();
		
			String upperCase = name.substring(0, 1).toUpperCase();
			String on = "on" + upperCase + name.substring(1);
			Method findMethod = ReflectionUtils.findMethod(this.getClass(), on, Map.class,resolve);
			
			if (findMethod != null) {

				AllowFields allowAnnotation = findMethod.getAnnotation(AllowFields.class);
				if (allowAnnotation != null) {
					allowField(bodyToMap, allowAnnotation.value());
				} else {
					ForbidFields forbidAnnotation = findMethod.getAnnotation(ForbidFields.class);
					if (forbidAnnotation != null) {
						forbidField(bodyToMap, forbidAnnotation.value());
					}
				}

				AllowCondition allowCondition = findMethod.getAnnotation(AllowCondition.class);
				if (allowCondition != null) {
					allowCondition(oldInstance, allowCondition.value());
				}
				
				try {
					findMethod.invoke(this,bodyToMap, oldInstance);
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
//				throw new ServerError500Exception("不支持"+name+"事件");
			}
		
	}
	

	private void allowCondition(Object source, String condition) {
		if(source==null) {
			return;
		}
		boolean _allowCondition = _allowCondition(source, condition);
		if(!_allowCondition) {
			Class<? extends Object> class1 = source.getClass();
			String name = SmartQuery.getStructure(class1).getName();
			throw new AccessDeny403Exception(name + " 非法操作, " + condition);
		}
		
	}
	
	public boolean _allowCondition(Object source, String condition) {
		if(condition==null || "".equals(condition)) {
			return true;
		}
		ExpressionParser parser = new SpelExpressionParser();
		EvaluationContext context = new StandardEvaluationContext(); 
		context.setVariable("old", source);
		return parser.parseExpression(condition, new TemplateParserContext())
				.getValue(context, Boolean.class);
	}
//	
//	public boolean _allowCondition(Object source, String condition) {
//		if(condition==null || "".equals(condition)) {
//			return true;
//		}
//		SpelContext spelContext = new SpelContext(source);
//		ExpressionParser parser = new SpelExpressionParser();
//		return parser.parseExpression(condition, new TemplateParserContext())
//				.getValue(spelContext, Boolean.class);
//	}

	private void forbidField(Map<String, Object> bodyToMap, String[] filterField) {
		
		Set<String> keySet = bodyToMap.keySet();
		for (String str : filterField) {
			if(keySet.contains(str)) {
				throw new AccessDeny403Exception("非法字段"+ str);
			}
		}
	}

	private void allowField(Map<String, Object> bodyToMap, String[] allowField) {
		
		Set<String> keySet = bodyToMap.keySet();
		HashSet<String> allowSet = new HashSet();
		allowSet.add("uuid");
		for (String str : allowField) {
			allowSet.add(str);
		}
		if(!allowSet.containsAll(keySet)) {
			throw new AccessDeny403Exception("非法字段");
		}
	}
}
