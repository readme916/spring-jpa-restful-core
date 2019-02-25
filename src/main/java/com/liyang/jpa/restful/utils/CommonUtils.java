package com.liyang.jpa.restful.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtils {


	public static String object2String(Object object) {

		ObjectMapper objectMapper = new ObjectMapper();
		String writeValueAsString=null;
		try {
			writeValueAsString = objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writeValueAsString;
	}
	public static <T> T string2Object(String jsonString, Class<T> clazz) {

	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
	      return (T) objectMapper.readValue(jsonString, clazz);
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    return null;
	  }
	 public static HashMap<String,String> validate(Object object) {

			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			Validator validator = factory.getValidator();
			Set<ConstraintViolation<Object>> validates = validator.validate(object);
			
			HashMap<String,String> errors = new HashMap<String,String>();
			if (!validates.isEmpty()) {
				for (ConstraintViolation<Object> constraint : validates) {
					ValidateError validateError = new ValidateError();
					validateError.setMessage(constraint.getMessage());
					validateError.setProperty(constraint.getPropertyPath().toString());
					errors.put(constraint.getPropertyPath().toString(),constraint.getMessage());
				}
			}
			return errors;
		}
	 public static class ValidateError {

			public String message;
			public String property;
			public String getMessage() {
				return message;
			}
			public void setMessage(String message) {
				this.message = message;
			}
			public String getProperty() {
				return property;
			}
			public void setProperty(String property) {
				this.property = property;
			}
			
		}
	 
	 public static String[] getNullPropertyNames (Object source) {
	        final BeanWrapper src = new BeanWrapperImpl(source);
	        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

	        Set<String> emptyNames = new HashSet<String>();
	        for(java.beans.PropertyDescriptor pd : pds) {
	            Object srcValue = src.getPropertyValue(pd.getName());
	            if (srcValue == null) emptyNames.add(pd.getName());
	        }
	        String[] result = new String[emptyNames.size()];
	        return emptyNames.toArray(result);
	    }
		
		public static Set<String> getNotNullPropertyNames (Object source) {
	        final BeanWrapper src = new BeanWrapperImpl(source);
	        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
	        Set<String> notEmptyNames = new HashSet<String>();
	        for(java.beans.PropertyDescriptor pd : pds) {
	            Object srcValue = src.getPropertyValue(pd.getName());
	            if (srcValue != null) notEmptyNames.add(pd.getName());
	        }
	        return notEmptyNames;
	    }

	    public static void copyPropertiesIgnoreNull(Object src, Object target){
	        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
	        
	    }
	    public static boolean isPackageClassObject(Object obj) {
			try {
				return ((Class<?>)obj.getClass().getField("TYPE").get(null)).isPrimitive();
			} catch (Exception e) {
				return false;
			}
		}
	    public static boolean isPackageClass(Class<?> cls) {
			try {
				return ((Class<?>)cls.getField("TYPE").get(null)).isPrimitive();
			} catch (Exception e) {
				return false;
			}
		}

	    
}
