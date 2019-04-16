package com.liyang.jpa.restful.core.annotation.display;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.liyang.jpa.restful.core.annotation.display.ListFilter.FormItem;
import com.liyang.jpa.restful.core.annotation.display.ListFilter.Relationship;

@Retention(RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface FieldDisplays {

	FieldDisplay[] value();

}