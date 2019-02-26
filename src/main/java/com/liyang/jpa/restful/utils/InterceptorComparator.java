package com.liyang.jpa.restful.utils;

import java.util.Comparator;

import com.liyang.jpa.restful.interceptor.JpaRestfulGetInterceptor;
import com.liyang.jpa.restful.interceptor.JpaRestfulInterceptor;

public class InterceptorComparator implements Comparator<JpaRestfulInterceptor >{
    @Override
     public int compare(JpaRestfulInterceptor o1, JpaRestfulInterceptor o2) {
     return o1.order()-o2.order();
    }
}
