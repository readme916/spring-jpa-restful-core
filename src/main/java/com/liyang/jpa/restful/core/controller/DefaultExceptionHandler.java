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

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.liyang.jpa.restful.core.exception.AccessDeny403Exception;
import com.liyang.jpa.restful.core.exception.JpaRestfulException;
import com.liyang.jpa.restful.core.exception.Validator422Exception;
import com.liyang.jpa.restful.core.utils.CommonUtils;
import com.liyang.jpa.restful.core.utils.CommonUtils.ValidateError;

public interface DefaultExceptionHandler {
	@ExceptionHandler(JpaRestfulException.class)
	default void customerExceptionHandler(JpaRestfulException ex, HttpServletResponse httpResponse) {
		try {
			httpResponse.setContentType("application/json; charset=utf-8");
			Response response = new Response(ex);
			String objectToString = CommonUtils.objectToString(response);
			httpResponse.getWriter().write(objectToString);
			httpResponse.setStatus(ex.getStatus());
			httpResponse.flushBuffer();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@ExceptionHandler(TransactionSystemException.class)
	default void validatorHandler(TransactionSystemException ex, HttpServletResponse httpResponse) {

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
			try {
				httpResponse.setContentType("application/json; charset=utf-8");
				Validator422Exception validator422Exception = new Validator422Exception(errors);
				Response response = new Response(validator422Exception);
				String objectToString = CommonUtils.objectToString(response);
				httpResponse.getWriter().write(objectToString);
				httpResponse.setStatus(validator422Exception.getStatus());
				httpResponse.flushBuffer();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@ExceptionHandler(Exception.class)
	default void ExceptionHandler(Exception ex, HttpServletResponse httpResponse) {
		ex.printStackTrace();
		try {
			httpResponse.setContentType("application/json; charset=utf-8");
			Response response = new Response(ex);
			String objectToString = CommonUtils.objectToString(response);
			httpResponse.getWriter().write(objectToString);
			httpResponse.setStatus(500);
			httpResponse.flushBuffer();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public static class Response{

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
			ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
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
