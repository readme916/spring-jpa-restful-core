package com.liyang.jpa.restful.core.utils;

import java.util.HashSet;

import com.liyang.jpa.restful.core.event.EventManager;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;

public class EntityStructureEx extends EntityStructure {

	private HashSet<EntityEvent> events = new HashSet();
	
	private EventManager eventManager;
	
	public HashSet<EntityEvent> getEvents() {
		return events;
	}

	public void setEvents(HashSet<EntityEvent> events) {
		this.events = events;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public void setEventManager(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	public static class EntityEvent{
		private HashSet<String> fields;
		private String condition;
		private String name;
		private HashSet<String> roles;
		
		public HashSet<String> getRoles() {
			return roles;
		}
		public void setRoles(HashSet<String> roles) {
			this.roles = roles;
		}
		public HashSet<String> getFields() {
			return fields;
		}
		public void setFields(HashSet<String> fields) {
			this.fields = fields;
		}
		public String getCondition() {
			return condition;
		}
		public void setCondition(String condition) {
			this.condition = condition;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
	}
	
}
