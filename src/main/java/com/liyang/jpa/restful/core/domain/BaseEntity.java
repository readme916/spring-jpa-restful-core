package com.liyang.jpa.restful.core.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

public interface BaseEntity extends Serializable {

    
	public  String getUuid();
	
	public  void setUuid(String uuid);
		
	public  String getEvent();

	public  void setEvent(String event);

	public String getCreatedBy() ;

	public void setCreatedBy(String createdBy) ;

	public String getModifiedBy();

	public void setModifiedBy(String modifiedBy) ;

	public Date getCreatedAt() ;

	public void setCreatedAt(Date createdAt) ;

	public Date getModifiedAt() ;

	public void setModifiedAt(Date modifiedAt);
	
}
