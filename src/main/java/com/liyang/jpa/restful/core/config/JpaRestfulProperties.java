package com.liyang.jpa.restful.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.jpa.restful")
public class JpaRestfulProperties {

	private String path;
	private String structurePath;
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getStructurePath() {
		return structurePath;
	}
	public void setStructurePath(String structurePath) {
		this.structurePath = structurePath;
	}
	
	
}
