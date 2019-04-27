package com.liyang.jpa.restful.core.domain;

public interface ImageEntity extends BaseEntity {
	public String getName();

	public void setName(String name);

	public Long getSize();

	public void setSize(Long size);

	public String getFormat();

	public void setFormat(String format);

	public String getUrl();

	public void setUrl(String url);

	public String getWidth();

	public void setWidth(String width);

	public String getHeight();

	public void setHeight(String height);
}
