package com.liyang.jpa.restful.core.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.mysql.exception.GetFormatException;
import com.liyang.jpa.restful.core.exception.BusinessException;
import com.liyang.jpa.restful.core.exception.PostFormatException;

@RestController
@ControllerAdvice
public class ErrorController {
 
    @ExceptionHandler(BusinessException.class)
    public Map<String,Object> customerExceptionHandler(BusinessException ex){
        Map<String,Object> res = new HashMap<>();
        res.put("error",ex.getError());
        res.put("code",ex.getCode());
        res.put("message",ex.getMessage());
        res.put("cause",ex.getBecause());
        return res;
    }
    @ExceptionHandler(GetFormatException.class)
    public Map<String,Object> customerExceptionHandler(GetFormatException ex){
        Map<String,Object> res = new HashMap<>();
        res.put("error",ex.getError());
        res.put("code",ex.getCode());
        res.put("message",ex.getMessage());
        res.put("cause",ex.getBecause());
        return res;
    }
    @ExceptionHandler(PostFormatException.class)
    public Map<String,Object> customerExceptionHandler(PostFormatException ex){
        Map<String,Object> res = new HashMap<>();
        res.put("error",ex.getError());
        res.put("code",ex.getCode());
        res.put("message",ex.getMessage());
        res.put("cause",ex.getBecause());
        return res;
    }
}
