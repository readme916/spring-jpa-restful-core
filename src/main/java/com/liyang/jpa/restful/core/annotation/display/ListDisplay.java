package com.liyang.jpa.restful.core.annotation.display;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface ListDisplay {
	String label();
	String field() default "";
	int order();

}
