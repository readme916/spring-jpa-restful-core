package com.liyang.jpa.restful.core.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

public interface BaseEntity extends Serializable {

	public String getOperate();

	public void setOperate(String operate); 

	public String getUuid();

	public void setUuid(String uuid);

	
}
