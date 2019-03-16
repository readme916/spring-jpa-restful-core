package com.liyang.jpa.restful.core.event;

import java.util.Map;

import org.springframework.context.ApplicationEvent;

import com.liyang.jpa.smart.query.db.structure.EntityStructure;


public class RestfulEvent extends ApplicationEvent {

	private String event;
	private  EntityStructure entityStructure;
	
	public RestfulEvent(String event, Object source, EntityStructure entityStructure) {
		super(source);
		this.event = event;
		this.entityStructure = entityStructure;
	}

	public EntityStructure getEntityStructure() {
		return entityStructure;
	}

	public void setEntityStructure(EntityStructure entityStructure) {
		this.entityStructure = entityStructure;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

}
