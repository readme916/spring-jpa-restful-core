package com.liyang.jpa.restful.core.exception;

/**
 * 使用jparestful的post方法时候的json格式和json to object时候的错误
 * @author liyang
 *
 */
public class JsonFormat406Exception extends JpaRestfulException{

	
	public JsonFormat406Exception(Object because) {
		super(406, "Not Acceptable" , "JSON格式错误", because);
		// TODO Auto-generated constructor stub
	}
	

}
