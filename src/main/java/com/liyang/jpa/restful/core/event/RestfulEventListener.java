package com.liyang.jpa.restful.core.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import com.liyang.jpa.restful.core.exception.JpaRestfulException;

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
					try {
						findMethod.invoke(this, event.getSource());
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						Throwable cause = e.getCause();
						if( cause instanceof JpaRestfulException) {
							JpaRestfulException ex = (JpaRestfulException)cause;
							throw ex;
						}else {
							e.printStackTrace();
						}
					}
			}
		}
	}
}
