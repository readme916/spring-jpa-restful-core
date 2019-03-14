package com.liyang.jpa.restful.core.exception;

/**
 * 使用jparestful的post方法时候的validator的错误
 * @author liyang
 *
 */
public class Validator422Exception extends JpaRestfulException{

	public Validator422Exception(Object because) {
		super(422, "Unprocessable Entity" , "格式验证错误", because);
		// TODO Auto-generated constructor stub
	}
	
	public Validator422Exception() {
		super(422, "Unprocessable Entity" , "格式验证错误", "");
		// TODO Auto-generated constructor stub
	}
}
