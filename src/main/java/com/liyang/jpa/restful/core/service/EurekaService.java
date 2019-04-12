package com.liyang.jpa.restful.core.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.liyang.jpa.restful.core.response.HTTPPostOkResponse;

@Component
@ConditionalOnProperty(name = "eureka.client.serviceUrl.defaultZone")
public class EurekaService {


	@Value("${eureka.client.serviceUrl.defaultZone}")
	private String defaultZone;
	
	@Value("${spring.application.name}")
	private String app;
	
	public Map getStatus() {
		RestTemplate restT = new RestTemplate();
		Map ret = restT.getForObject(defaultZone+"apps/"+app, Map.class);
		
		HashMap<String,String> hashMap = new HashMap<String,String>();
		hashMap.put("application", ((Map)ret.get("application")).get("name").toString());
		hashMap.put("count", String.valueOf(((List)((Map)ret.get("application")).get("instance")).size()));
		
		List list = ((List)((Map)ret.get("application")).get("instance"));
		hashMap.put("status", ((Map)list.get(0)).get("status").toString());
		return hashMap;
	}


	public Object offline() {
		RestTemplate restT = new RestTemplate();
		Map ret = restT.getForObject(defaultZone+"apps/"+app, Map.class);
		List list = ((List)((Map)ret.get("application")).get("instance"));
		for (Object l : list) {
			String id = ((Map)l).get("instanceId").toString();
			RequestEntity<String> requestEntity;
			try {
				requestEntity = RequestEntity.put(new URI(defaultZone+"apps/"+app+"/"+id+"/status?value=OUT_OF_SERVICE")).contentType(MediaType.APPLICATION_JSON).body("");
				restT.exchange(requestEntity, String.class);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new HTTPPostOkResponse();
	}

	public Object online() {
		RestTemplate restT = new RestTemplate();
		Map ret = restT.getForObject(defaultZone+"apps/"+app, Map.class);
		List list = ((List)((Map)ret.get("application")).get("instance"));
		for (Object l : list) {
			String id = ((Map)l).get("instanceId").toString();
			RequestEntity<String> requestEntity;
			try {
				requestEntity = RequestEntity.put(new URI(defaultZone+"apps/"+app+"/"+id+"/status?value=UP")).contentType(MediaType.APPLICATION_JSON).body("");
				restT.exchange(requestEntity, String.class);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new HTTPPostOkResponse();
	}
}
