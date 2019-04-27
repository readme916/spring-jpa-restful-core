package com.liyang.jpa.restful.core.domain;

public interface FileEntity extends BaseEntity {
	public String getName();

	public void setName(String name);

	public Long getSize();

	public void setSize(Long size);

	public String getFormat();

	public void setFormat(String format);

	public String getUrl();

	public void setUrl(String url);
}
