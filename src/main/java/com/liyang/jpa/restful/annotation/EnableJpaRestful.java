package com.liyang.jpa.restful.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.liyang.jpa.mysql.annotation.EnableJpaSmartQuery;
import com.liyang.jpa.mysql.config.ApplicationContextSupport;
import com.liyang.jpa.mysql.config.JpaSmartQuerySupport;
import com.liyang.jpa.restful.config.JpaRestfulSupport;

@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@EnableJpaSmartQuery
@Import({ApplicationContextSupport.class,JpaSmartQuerySupport.class,JpaRestfulSupport.class})
public @interface EnableJpaRestful {

}
