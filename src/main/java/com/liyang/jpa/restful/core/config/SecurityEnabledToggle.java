package com.liyang.jpa.restful.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.liyang.jpa.restful.core.controller.StructureShowController;

@Configuration
@ConditionalOnProperty(value = "spring.jpa.restful.security.enabled", havingValue = "false")
@ConditionalOnBean(value = StructureShowController.class)
@Order(-1)
public class SecurityEnabledToggle extends WebSecurityConfigurerAdapter {

	@Value("${spring.jpa.restful.structure-path}")
	private String structurePath;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.requestMatchers().antMatchers("/" + structurePath + "/**").and().authorizeRequests()
				.antMatchers("/" + structurePath + "/**").permitAll();
	}

}
