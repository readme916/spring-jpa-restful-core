package com.liyang.jpa.restful.core.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.liyang.jpa.restful.core.config.JpaRestfulAutoConfiguration;

@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({JpaRestfulAutoConfiguration.class})
public @interface EnableJpaRestful {

}
