package com.liyang.jpa.restful.core.utils;

import java.util.HashSet;
import java.util.Set;

import com.liyang.jpa.restful.core.annotation.display.Position;
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
		private Set<String> fields;
		private String condition;
		private String name;
		private String label;
		private Set<String> roles;
		private int order=100;
		private boolean display;
		
		public boolean isDisplay() {
			return display;
		}
		public void setDisplay(boolean display) {
			this.display = display;
		}
		public int getOrder() {
			return order;
		}
		public void setOrder(int order) {
			this.order = order;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public Set<String> getRoles() {
			return roles;
		}
		public void setRoles(Set<String> roles) {
			this.roles = roles;
		}
		public Set<String> getFields() {
			return fields;
		}
		public void setFields(Set<String> fields) {
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
