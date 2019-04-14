package com.liyang.jpa.restful.core.annotation.display;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@Repeatable(value = ListFilters.class)
public @interface ListFilter {
	Relationship relationship() default Relationship.EQ;
	String field() default "";
	String label();
	int order();
	FormItem formItem() default FormItem.INPUT;
	
	public enum Relationship{
		EQ,GT,GTE,LT,LTE,LIKE,IN,NOT,OR
	}
	
	public enum FormItem{
		INPUT,CHECKBOX,RADIO,SELECT,TIME_PICKER,DATE_PICKER
	}
	

}
