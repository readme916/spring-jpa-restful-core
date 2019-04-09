package com.liyang.jpa.restful.core.utils;


public class SpelContext {

	private Object old;

	public Object getOld() {
		return old;
	}

	public void setOld(Object old) {
		this.old = old;
	}

	public SpelContext(Object old) {
		super();
		this.old = old;
	}

}
