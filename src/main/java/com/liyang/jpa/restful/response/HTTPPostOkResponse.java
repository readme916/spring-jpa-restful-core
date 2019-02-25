package com.liyang.jpa.restful.response;

import java.util.Date;

public class HTTPPostOkResponse {

	private int error=0;
	private String uuid;
	public int getError() {
		return error;
	}
	public void setError(int error) {
		this.error = error;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
}
