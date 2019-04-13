package com.liyang.jpa.restful.core.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface ListDisplay {

	boolean display() default true;
	String nickname();
	String targetObjectField();
	int order();

}
