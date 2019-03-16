package com.liyang.jpa.restful.core.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.liyang.jpa.restful.core.exception.AccessDeny403Exception;
import com.liyang.jpa.restful.core.exception.Business503Exception;
import com.liyang.jpa.restful.core.exception.JpaRestfulException;
import com.liyang.jpa.restful.core.exception.JsonFormat406Exception;
import com.liyang.jpa.restful.core.exception.NotFound404Exception;
import com.liyang.jpa.restful.core.exception.ServerError500Exception;
import com.liyang.jpa.restful.core.exception.Timeout408Exception;
import com.liyang.jpa.restful.core.exception.Validator422Exception;
import com.liyang.jpa.restful.core.utils.CommonUtils;
import com.liyang.jpa.restful.core.utils.CommonUtils.ValidateError;

public abstract class DefaultExceptionHandler {
	@ExceptionHandler(AccessDeny403Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public Object customerExceptionHandler1(AccessDeny403Exception ex) {
		Response response = new Response(ex);
		return response;

	}

	@ExceptionHandler(Business503Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public Object customerExceptionHandler2(Business503Exception ex) {
		Response response = new Response(ex);
		return response;

	}

	@ExceptionHandler(JsonFormat406Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	public Object customerExceptionHandler3(JsonFormat406Exception ex) {
		Response response = new Response(ex);
		return response;

	}

	@ExceptionHandler(NotFound404Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Object customerExceptionHandler4(NotFound404Exception ex) {
		Response response = new Response(ex);
		return response;

	}

	@ExceptionHandler(ServerError500Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Object customerExceptionHandler5(ServerError500Exception ex) {
		Response response = new Response(ex);
		return response;

	}

	@ExceptionHandler(Timeout408Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
	public Object customerExceptionHandler6(Timeout408Exception ex) {
		Response response = new Response(ex);
		return response;

	}

	@ExceptionHandler(Validator422Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public Object customerExceptionHandler7(Validator422Exception ex) {
		Response response = new Response(ex);
		return response;

	}

	@ExceptionHandler(TransactionSystemException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public Object validatorHandler(TransactionSystemException ex) {
		Throwable rootCause = ex.getRootCause();
		if (rootCause instanceof ConstraintViolationException) {
			ConstraintViolationException rootEx = (ConstraintViolationException) rootCause;
			Set<ConstraintViolation<?>> constraintViolations = rootEx.getConstraintViolations();
			HashMap<String, String> errors = new HashMap<String, String>();
			if (!constraintViolations.isEmpty()) {
				for (ConstraintViolation<?> constraint : constraintViolations) {
					ValidateError validateError = new ValidateError();
					validateError.setMessage(constraint.getMessage());
					validateError.setProperty(constraint.getPropertyPath().toString());
					errors.put(constraint.getPropertyPath().toString(), constraint.getMessage());
				}
			}
			Validator422Exception validator422Exception = new Validator422Exception(errors);
			Response response = new Response(validator422Exception);
			return response;
		}else {
			ex.printStackTrace();
			return null;
		}
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public Object ExceptionHandler(DataIntegrityViolationException ex) {
		Business503Exception business503Exception = new Business503Exception(503,"唯一索引不允许重复","");
		Response response = new Response(business503Exception);
		return response;
	}
	@ExceptionHandler(Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Object ExceptionHandler(Exception ex) {
		ex.printStackTrace();
		Response response = new Response(ex);
		return response;
	}

	public static class Response {

		private int status;
		private String error;
		@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
		private Date timestamp;
		private String path;
		private String message;
		private Object detail;

		public Response(JpaRestfulException ex) {
			this.status = ex.getStatus();
			this.error = ex.getError();
			this.timestamp = ex.getTimestamp();
			this.path = ex.getPath();
			this.message = ex.getMessage();
			this.detail = ex.getDetail();
		}

		public Response(Exception ex) {
			this.status = 500;
			this.error = "内部错误";
			this.timestamp = new Date();
			ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
					.getRequestAttributes();
			HttpServletRequest request = requestAttributes.getRequest();
			String pathInfo = request.getRequestURI();
			this.path = pathInfo;
			this.message = ex.getMessage();
			this.detail = "";
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Object getDetail() {
			return detail;
		}

		public void setDetail(Object detail) {
			this.detail = detail;
		}

	}
}
