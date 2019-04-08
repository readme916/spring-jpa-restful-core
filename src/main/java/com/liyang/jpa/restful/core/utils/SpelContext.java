package com.liyang.jpa.restful.core.utils;


public class SpelContext {

	private Object entity;

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}

	public SpelContext(Object entity) {
		super();
		this.entity = entity;
	}
	
}
