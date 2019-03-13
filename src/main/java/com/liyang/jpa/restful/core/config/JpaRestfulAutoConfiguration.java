package com.liyang.jpa.restful.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.liyang.jpa.smart.query.config.JpaSmartQueryAutoConfiguration;

@ComponentScan({"com.liyang.jpa.restful.core.controller","com.liyang.jpa.restful.core.service"})
@Configuration
@Import(JpaSmartQueryAutoConfiguration.class)
public class JpaRestfulAutoConfiguration {

}
