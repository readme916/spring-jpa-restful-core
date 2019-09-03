package com.liyang.jpa.restful.core.controller;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liyang.jpa.restful.core.annotation.JpaRestfulResource;
import com.liyang.jpa.restful.core.annotation.display.FieldDisplay;
import com.liyang.jpa.restful.core.controller.StructureShowController.ResourceUrl;
import com.liyang.jpa.restful.core.interceptor.JpaRestfulDeleteInterceptor;
import com.liyang.jpa.restful.core.interceptor.JpaRestfulGetInterceptor;
import com.liyang.jpa.restful.core.interceptor.JpaRestfulPostInterceptor;
import com.liyang.jpa.restful.core.utils.CommonUtils;
import com.liyang.jpa.restful.core.utils.EntityStructureEx.EntityEvent;
import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.BaseEnum;
import com.liyang.jpa.smart.query.db.structure.ColumnJoinType;
import com.liyang.jpa.smart.query.db.structure.ColumnStucture;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;

@Controller
@RequestMapping("${spring.jpa.restful.structure-path}")
@ConditionalOnProperty(name = { "spring.jpa.restful.structure-path", "spring.jpa.restful.path" })
public class StructureShowController extends DefaultExceptionHandler {
	protected final static Logger logger = LoggerFactory.getLogger(StructureShowController.class);

	@Value(value = "${spring.jpa.restful.path}")
	private String configPath;

	@Value(value = "${spring.jpa.restful.structure-path}")
	private String configStructurePath;

	@Autowired(required = false)
	private List<JpaRestfulGetInterceptor> gets;

	@Autowired(required = false)
	private List<JpaRestfulPostInterceptor> posts;

	@Autowired(required = false)
	private List<JpaRestfulDeleteInterceptor> deletes;

	private String getStructurePath(HttpServletRequest request) {
		if (request.getHeader("x-forwarded-prefix") != null) {
			return request.getHeader("x-forwarded-prefix") + "/" + this.configStructurePath + "/" + this.configPath;
		} else {
			return "/" + this.configStructurePath + "/" + this.configPath;
		}
	}

	private String getPath(HttpServletRequest request) {
		if (request.getHeader("x-forwarded-prefix") != null) {
			return request.getHeader("x-forwarded-prefix") + "/" + this.configPath;
		} else {
			return "/" + this.configPath;
		}
	}

	@ModelAttribute
	public void populateModel(Model model, HttpServletRequest request) {

		ArrayList<SimpleResource> arrayList = new ArrayList<SimpleResource>();
		HashMap<String, EntityStructure> nametostructure = SmartQuery.getNametostructure();
		Collection<EntityStructure> values = nametostructure.values();
		for (EntityStructure entityStructure : values) {
			Class<?> cls = entityStructure.getEntityClass();
			if (cls.isAnnotationPresent(JpaRestfulResource.class)) {
				SimpleResource simpleResource = new SimpleResource();
				simpleResource.setName(entityStructure.getName());
				simpleResource.setResourceUri(getPath(request) + "/" + entityStructure.getName());
				simpleResource.setStructureUri(getStructurePath(request) + "/" + entityStructure.getName());
				arrayList.add(simpleResource);
			}
		}
		List<SimpleResource> collect = arrayList.stream().sorted(new Comparator<SimpleResource>() {

			@Override
			public int compare(SimpleResource o1, SimpleResource o2) {
				// TODO Auto-generated method stub
				return o1.getName().compareTo(o2.getName());
			}
		}).collect(Collectors.toList());

		model.addAttribute("resourceList", collect);
	}

	@RequestMapping(path = "", method = RequestMethod.GET)
	public String resource() throws JsonProcessingException {
		return "restful_home";
	}

	@RequestMapping(path = "${spring.jpa.restful.path}/{resource}", method = RequestMethod.GET)
	public String resource(@PathVariable String resource, Model model, HttpServletRequest request)
			throws JsonProcessingException {
		Class<?> entityClass = SmartQuery.getStructure(resource).getEntityClass();
		FullResource fullResource = new FullResource();

		ResourceUrl relativeResource = new ResourceUrl();
		relativeResource.setResourceUri(getPath(request) + "/" + resource + "/{id}");
		relativeResource.setStructureUri(getStructurePath(request) + "/" + resource + "/{id}");
		fullResource.getRelativeResource().add(relativeResource);
		ResourceUrl resourceUrl = new ResourceUrl();
		resourceUrl.setResourceUri(getPath(request) + "/" + resource);
		resourceUrl.setStructureUri(getStructurePath(request) + "/" + resource);
		fullResource.setResource(resourceUrl);
		fullResource.setTitle(resource + " - 主资源（列表）");
		MethodDescription getDescription = new MethodDescription();
		getDescription.setMethod("GET");
		getDescription.setDescription(
				"主资源" + resource + "的列表，可带格式查询参数和附加对象,例如:/user?fields=*,role&username[not]=&page=&size=&sort=");
		MethodDescription postDescription = new MethodDescription();
		postDescription.setMethod("POST");
		postDescription.setDescription("创建" + resource + "资源，格式见下");
		fullResource.setMethods(Arrays.asList(new MethodDescription[] { getDescription, postDescription }));
		fullResource
				.setInterceptors(_interceptorParse(fullResource.getResource().getResourceUri(), true, false, request));
		fullResource.setEvents(CommonUtils.getStructure(resource).getEvents());
		Field[] declaredFields = entityClass.getDeclaredFields();
		fillFields(declaredFields, fullResource, getPath(request) + "/" + resource,
				getStructurePath(request) + "/" + resource, true);

		HashMap<String, Object> postStructure = fullResource.getPostStructure();
		postStructure.remove("uuid");
		ObjectMapper mapper = new ObjectMapper();
		String writeValueAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(postStructure);
		fullResource.setPostStructureString(writeValueAsString);
		model.addAttribute("resource", fullResource);
		return "restful_structure";
	}

	@RequestMapping(path = "${spring.jpa.restful.path}/{resource}/{id}", method = RequestMethod.GET)
	public String resource(@PathVariable String resource, @PathVariable String id, Model model,
			HttpServletRequest request) throws JsonProcessingException {
		Class<?> entityClass = SmartQuery.getStructure(resource).getEntityClass();

		Field[] declaredFields = entityClass.getDeclaredFields();
		FullResource fullResource = new FullResource();
		Map<String, ColumnStucture> objectFields = SmartQuery.getStructure(entityClass).getObjectFields();
		Set<Entry<String, ColumnStucture>> entrySet = objectFields.entrySet();
		for (Entry<String, ColumnStucture> entry : entrySet) {
			ResourceUrl relativeResource = new ResourceUrl();
			relativeResource.setResourceUri(getPath(request) + "/" + resource + "/{id}/" + entry.getKey());
			relativeResource.setStructureUri(getStructurePath(request) + "/" + resource + "/{id}/" + entry.getKey());
			fullResource.getRelativeResource().add(relativeResource);
		}

		fullResource.setTitle(resource + " - 主资源（对象）");
		MethodDescription getDescription = new MethodDescription();
		getDescription.setMethod("GET");
		getDescription.setDescription("主资源" + resource + "的对象，可带附加对象,例如:user/1?fields=role,department,*");
		MethodDescription postDescription = new MethodDescription();
		postDescription.setMethod("POST");
		postDescription.setDescription("更新资源，格式见后");

		MethodDescription deleteDescription = new MethodDescription();
		deleteDescription.setMethod("DELETE");
		deleteDescription.setDescription("删除");

		fullResource.setMethods(
				Arrays.asList(new MethodDescription[] { getDescription, postDescription, deleteDescription }));
		ResourceUrl resourceUrl = new ResourceUrl();
		resourceUrl.setResourceUri(getPath(request) + "/" + resource + "/{id}");
		resourceUrl.setStructureUri(getStructurePath(request) + "/" + resource + "/{id}");
		fullResource.setResource(resourceUrl);
		fullResource
				.setInterceptors(_interceptorParse(fullResource.getResource().getResourceUri(), true, true, request));
		fullResource.setEvents(CommonUtils.getStructure(resource).getEvents());

		fillFields(declaredFields, fullResource, getPath(request) + "/" + resource,
				getStructurePath(request) + "/" + resource, true);
		HashMap<String, Object> postStructure = fullResource.getPostStructure();
		ObjectMapper mapper = new ObjectMapper();
		String writeValueAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(postStructure);
		fullResource.setPostStructureString(writeValueAsString);
		model.addAttribute("resource", fullResource);
		return "restful_structure";
	}

	@RequestMapping(path = "${spring.jpa.restful.path}/{resource}/{id}/{subResource}", method = RequestMethod.GET)
	public Object resource(@PathVariable String resource, @PathVariable String id, @PathVariable String subResource,
			Model model, HttpServletRequest request) throws JsonProcessingException {

		ColumnStucture columnStucture = SmartQuery.getStructure(resource).getObjectFields().get(subResource);
		boolean parentStructure = false;
		if (columnStucture.getJoinType().equals(ColumnJoinType.ONE_TO_MANY)
				|| columnStucture.getJoinType().equals(ColumnJoinType.ONE_TO_ONE)) {
			parentStructure = true;
		}

		Class<?> entityClass = columnStucture.getTargetEntity();

		Field[] declaredFields = entityClass.getDeclaredFields();

		FullResource fullResource = new FullResource();

		if (parentStructure) {
			ResourceUrl relativeResource = new ResourceUrl();
			relativeResource.setResourceUri(getPath(request) + "/" + resource + "/{id}/" + subResource + "/{id}");
			relativeResource
					.setStructureUri(getStructurePath(request) + "/" + resource + "/{id}/" + subResource + "/{id}");
			fullResource.getRelativeResource().add(relativeResource);
		}
		ResourceUrl resourceUrl = new ResourceUrl();
		resourceUrl.setResourceUri(getPath(request) + "/" + resource + "/{id}/" + subResource);
		resourceUrl.setStructureUri(getStructurePath(request) + "/" + resource + "/{id}/" + subResource);
		fullResource.setResource(resourceUrl);
		fullResource.setTitle(subResource + " - 桥接资源（列表）");
		fullResource.setEvents(CommonUtils.getStructure(entityClass).getEvents());
		ArrayList<MethodDescription> methods = new ArrayList<MethodDescription>();
		if (parentStructure) {
			MethodDescription getDescription = new MethodDescription();
			getDescription.setMethod("GET");
			getDescription.setDescription("桥接资源" + subResource + "的列表，可带格式查询参数和附加对象");
			methods.add(getDescription);
		}
		MethodDescription postDescription = new MethodDescription();
		postDescription.setMethod("POST");
		if (parentStructure) {
			postDescription.setDescription("给主资源" + resource + "的桥接资源" + subResource + "中新增对象,结构体不带id");
		} else {
			postDescription
					.setDescription("给主资源" + resource + "和桥接资源" + subResource + "中新增关联（多对多）或者改变关联（多对一）,结构体必须带id");
		}
		methods.add(postDescription);

		fullResource.setMethods(methods);
		fullResource.setInterceptors(
				_interceptorParse(fullResource.getResource().getResourceUri(), parentStructure, false, request));
		fillFields(declaredFields, fullResource, getPath(request) + "/" + resource + "/{id}/" + subResource,
				getStructurePath(request) + "/" + resource + "/{id}/" + subResource, parentStructure);
		HashMap<String, Object> postStructure = fullResource.getPostStructure();
		if (parentStructure) {
			postStructure.remove("uuid");
		}
		ObjectMapper mapper = new ObjectMapper();
		String writeValueAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(postStructure);
		fullResource.setPostStructureString(writeValueAsString);
		model.addAttribute("resource", fullResource);
		return "restful_structure";
	}

	@RequestMapping(path = "${spring.jpa.restful.path}/{resource}/{id}/{subResource}/{subId}", method = RequestMethod.GET)
	public Object resource(@PathVariable String resource, @PathVariable String id, @PathVariable String subResource,
			@PathVariable String subId, Model model, HttpServletRequest request) throws JsonProcessingException {
		ColumnStucture columnStucture = SmartQuery.getStructure(resource).getObjectFields().get(subResource);
		boolean parentStructure = false;
		if (columnStucture.getJoinType().equals(ColumnJoinType.ONE_TO_MANY)
				|| columnStucture.getJoinType().equals(ColumnJoinType.ONE_TO_ONE)) {
			parentStructure = true;
		}

		Class<?> entityClass = columnStucture.getTargetEntity();
		Field[] declaredFields = entityClass.getDeclaredFields();
		FullResource fullResource = new FullResource();
		EntityStructure structure = SmartQuery.getStructure(entityClass);
		Map<String, ColumnStucture> objectFields = structure.getObjectFields();
		Set<Entry<String, ColumnStucture>> entrySet = objectFields.entrySet();
		for (Entry<String, ColumnStucture> entry : entrySet) {
			ResourceUrl relativeResource = new ResourceUrl();
			relativeResource.setResourceUri(
					getPath(request) + "/" + resource + "/{id}/" + subResource + "/{id}/" + entry.getKey());
			relativeResource.setStructureUri(
					getStructurePath(request) + "/" + resource + "/{id}/" + subResource + "/{id}/" + entry.getKey());
			fullResource.getRelativeResource().add(relativeResource);
		}

		ResourceUrl resourceUrl = new ResourceUrl();
		resourceUrl.setResourceUri(getPath(request) + "/" + resource + "/{id}/" + subResource + "/{id}");
		resourceUrl.setStructureUri(getStructurePath(request) + "/" + resource + "/{id}/" + subResource + "/{id}");
		fullResource.setResource(resourceUrl);

		fullResource.setTitle(subResource + " - 桥接资源（对象）");
		fullResource.setEvents(CommonUtils.getStructure(entityClass).getEvents());
		ArrayList<MethodDescription> methods = new ArrayList<MethodDescription>();
		MethodDescription getDescription = new MethodDescription();
		getDescription.setMethod("GET");
		getDescription.setDescription("桥接资源" + subResource + "的对象，可带附加对象");
		methods.add(getDescription);

		MethodDescription postDescription = new MethodDescription();
		postDescription.setMethod("POST");
		postDescription.setDescription("更新子资源，格式见后");
		methods.add(postDescription);
		MethodDescription deleteDescription = new MethodDescription();
		deleteDescription.setMethod("DELETE");
		deleteDescription.setDescription("删除子资源");
		methods.add(deleteDescription);
		fullResource.setMethods(methods);

		fullResource.setInterceptors(
				_interceptorParse(fullResource.getResource().getResourceUri(), parentStructure, true, request));
		fillFields(declaredFields, fullResource, getPath(request) + "/" + resource + "/{id}/" + subResource,
				getStructurePath(request) + "/" + resource + "/{id}/" + subResource, parentStructure);
		HashMap<String, Object> postStructure = fullResource.getPostStructure();
		ObjectMapper mapper = new ObjectMapper();
		String writeValueAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(postStructure);
		fullResource.setPostStructureString(writeValueAsString);
		model.addAttribute("resource", fullResource);
		return "restful_structure";
	}

	@RequestMapping(path = "${spring.jpa.restful.path}/{resource}/{id}/{subResource}/{subId}/{subsubResource}", method = RequestMethod.GET)
	public Object resource(@PathVariable String resource, @PathVariable String id, @PathVariable String subResource,
			@PathVariable String subId, @PathVariable String subsubResource, Model model, HttpServletRequest request)
			throws JsonProcessingException {
		Class<?> entityClass = SmartQuery.getStructure(resource).getObjectFields().get(subResource).getTargetEntity();

		ColumnStucture columnStucture = SmartQuery.getStructure(entityClass).getObjectFields().get(subsubResource);
		boolean parentStructure = false;
		if (columnStucture.getJoinType().equals(ColumnJoinType.ONE_TO_MANY)
				|| columnStucture.getJoinType().equals(ColumnJoinType.ONE_TO_ONE)) {
			parentStructure = true;
		}

		entityClass = columnStucture.getTargetEntity();
		Field[] declaredFields = entityClass.getDeclaredFields();
		FullResource fullResource = new FullResource();

		EntityStructure structure = SmartQuery.getStructure(entityClass);
		fullResource.setEvents(CommonUtils.getStructure(entityClass).getEvents());
		ResourceUrl resourceUrl = new ResourceUrl();
		resourceUrl
				.setResourceUri(getPath(request) + "/" + resource + "/{id}/" + subResource + "/{id}/" + subsubResource);
		resourceUrl.setStructureUri(
				getStructurePath(request) + "/" + resource + "/{id}/" + subResource + "/{id}/" + subsubResource);
		fullResource.setResource(resourceUrl);

		fullResource.setTitle(subsubResource + " - 桥接资源（列表）");
		ArrayList<MethodDescription> methods = new ArrayList<MethodDescription>();
		if (parentStructure) {
			MethodDescription getDescription = new MethodDescription();
			getDescription.setMethod("GET");
			getDescription.setDescription("桥接资源" + subsubResource + "的列表，可带格式查询参数和附加对象");
			methods.add(getDescription);
		}
		MethodDescription postDescription = new MethodDescription();
		postDescription.setMethod("POST");
		if (parentStructure) {
			postDescription.setDescription("给桥接资源" + subsubResource + "中新增对象,结构体不带id");
		} else {
			postDescription.setDescription("给桥接资源" + subsubResource + "中新增关联（多对多）或者改变关联（多对一）,结构体必须带id");
		}
		methods.add(postDescription);

		fullResource.setMethods(methods);
		fullResource.setInterceptors(
				_interceptorParse(fullResource.getResource().getResourceUri(), parentStructure, false, request));
		fillFields(declaredFields, fullResource,
				getPath(request) + "/" + resource + "/{id}/" + subResource + "/{id}/" + subsubResource,
				getStructurePath(request) + "/" + resource + "/{id}/" + subResource + "/{id}/" + subsubResource, false);
		HashMap<String, Object> postStructure = fullResource.getPostStructure();
		if (parentStructure) {
			postStructure.remove("uuid");
		}
		ObjectMapper mapper = new ObjectMapper();
		String writeValueAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(postStructure);
		fullResource.setPostStructureString(writeValueAsString);
		model.addAttribute("resource", fullResource);
		return "restful_structure";
	}

	private void fillFields(Field[] declaredFields, FullResource fullResource, String resourcePath,
			String structurePath, boolean parentStructure) {
		for (Field field : declaredFields) {

			JsonIgnore ignoreAnnotation = field.getDeclaredAnnotation(JsonIgnore.class);
			if (ignoreAnnotation != null) {
				continue;
			}

			Transient transientAnnotation = field.getDeclaredAnnotation(Transient.class);
			if (field.getName().equals("serialVersionUID")

			) {
				continue;
			}
			ObjectProperty objectProperty = new ObjectProperty();
			if (field.getType() == String.class || field.getType() == Date.class || field.getType().isEnum()
					|| field.getType() == BigDecimal.class || CommonUtils.isPackageClass(field.getType())) {
				if (!field.getName().equals("createdAt") && !field.getName().equals("updatedAt")
						&& !field.getName().equals("modifiedAt") && !field.getName().equals("version")) {
					fullResource.getPostStructure().put(field.getName(), _defautlValue(field));
				}
			} else {
				if (parentStructure) {
					objectProperty.setResourceUri(resourcePath + "/{id}/" + field.getName());
					objectProperty.setStructureUri(structurePath + "/{id}/" + field.getName());
				}
			}
			if (transientAnnotation != null) {
				objectProperty.setLifeCycle(LifeCycle.TRANSIENT.desc);

			} else {
				objectProperty.setLifeCycle(LifeCycle.PERSISTENT.desc);
			}

			FieldDisplay fieldDisplayAnnotation = field.getDeclaredAnnotation(FieldDisplay.class);
			if (fieldDisplayAnnotation != null) {
				objectProperty.setLabel(fieldDisplayAnnotation.label());
				objectProperty.setTip(fieldDisplayAnnotation.tip());
				objectProperty.setOrder(fieldDisplayAnnotation.order());
			}

			objectProperty.setName(field.getName());
			objectProperty.setDataType(field.getGenericType().getTypeName().replace("java.util.", "")
					.replace("java.lang.", "").replace("com.utopia.tokensart.common.models.", ""));
			objectProperty.setConstraints(_constraintParse(field));
			if (field.getType().isEnum()) {
				ArrayList<String> arrayList = new ArrayList<String>();
				BaseEnum[] enumConstants = (BaseEnum[]) field.getType().getEnumConstants();
				for (BaseEnum enu : enumConstants) {
					arrayList.add(enu.toString() + " ( " + enu.getLabel() + " ) ");
				}

				objectProperty.setConstraints(arrayList);
			}
			objectProperty.setRelationship(_relationshipParse(field));
			fullResource.getFields().add(objectProperty);

		}
		List<Object> collect = fullResource.getFields().stream().sorted(new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				return ((ObjectProperty)o1).order -  ((ObjectProperty)o2).order;
			}
			
		}).collect(Collectors.toList());
		
		
		fullResource.setFields(collect);
	}

	private Object _defautlValue(Field field) {
		if (field.getType() == String.class) {
			return "";
		} else if (field.getType() == Boolean.class) {
			return false;
		} else if (field.getType() == Character.class) {
			return "";
		} else if (field.getType().isEnum()) {
			return "";
		} else {
			return 0;
		}
	}

	private HashMap<String, List<Interceptor>> _interceptorParse(String resourceUri, boolean parentStructure,
			boolean delete, HttpServletRequest request) {
		String replace = resourceUri.replace(getPath(request), "");

		ArrayList<Interceptor> getList = new ArrayList<Interceptor>();
		ArrayList<Interceptor> postList = new ArrayList<Interceptor>();
		ArrayList<Interceptor> deleteList = new ArrayList<Interceptor>();

		if (this.gets != null) {
			for (JpaRestfulGetInterceptor interceptor : this.gets) {
				if (_match(interceptor.path(), replace)) {
					getList.add(new Interceptor(interceptor.name(), interceptor.description(),
							String.join(",", interceptor.path()), interceptor.order()));
				}
			}
		}
		if (this.posts != null) {
			for (JpaRestfulPostInterceptor interceptor : this.posts) {
				if (_match(interceptor.path(), replace)) {
					postList.add(new Interceptor(interceptor.name(), interceptor.description(),
							String.join(",", interceptor.path()), interceptor.order()));
				}
			}
		}
		if (this.deletes != null) {
			for (JpaRestfulDeleteInterceptor interceptor : this.deletes) {
				if (_match(interceptor.path(), replace)) {
					deleteList.add(new Interceptor(interceptor.name(), interceptor.description(),
							String.join(",", interceptor.path()), interceptor.order()));
				}
			}
		}
		HashMap<String, List<Interceptor>> hashMap = new HashMap<String, List<Interceptor>>();
		if (parentStructure) {
			hashMap.put("GET", getList);
		}
		hashMap.put("POST", postList);
		if (delete) {
			hashMap.put("DELETE", deleteList);
		}
		return hashMap;
	}

	private boolean _match(String[] pattern, String path) {
		PathMatcher matcher = new AntPathMatcher();
		for (String str : pattern) {
			if (matcher.match(str, path)) {
				return true;
			}
		}
		return false;
	}

	private String _relationshipParse(Field field) {
		ManyToOne manyToOneAnnotation = field.getDeclaredAnnotation(ManyToOne.class);
		ManyToMany manyToManyAnnotation = field.getDeclaredAnnotation(ManyToMany.class);
		OneToOne oneToOneAnnotation = field.getDeclaredAnnotation(OneToOne.class);
		OneToMany oneToManyAnnotation = field.getDeclaredAnnotation(OneToMany.class);
		if (manyToOneAnnotation != null) {
			return "多对一";
		} else if (manyToManyAnnotation != null) {
			return "多对多";
		} else if (oneToOneAnnotation != null) {
			return "一对一";
		} else if (oneToManyAnnotation != null) {
			return "一对多";
		}
		return null;
	}

	private ArrayList<String> _constraintParse(Field field) {
		NotNull notNullAnnotation = field.getDeclaredAnnotation(NotNull.class);
		Min minAnnotation = field.getDeclaredAnnotation(Min.class);
		Max maxAnnotation = field.getDeclaredAnnotation(Max.class);
		Digits DigitsAnnotation = field.getDeclaredAnnotation(Digits.class);
		Size sizeAnnotation = field.getDeclaredAnnotation(Size.class);
		Past pastAnnotation = field.getDeclaredAnnotation(Past.class);
		Future futureAnnotation = field.getDeclaredAnnotation(Future.class);
		NotBlank notBlankAnnotation = field.getDeclaredAnnotation(NotBlank.class);
		Length lengthAnnotation = field.getDeclaredAnnotation(Length.class);
		NotEmpty notEmptyAnnotation = field.getDeclaredAnnotation(NotEmpty.class);
		Range rangeAnnotation = field.getDeclaredAnnotation(Range.class);
		Email emailAnnotation = field.getDeclaredAnnotation(Email.class);
		Pattern patternAnnotation = field.getDeclaredAnnotation(Pattern.class);

		ArrayList<String> arrayList = new ArrayList<String>();

		if (notNullAnnotation != null) {
			arrayList.add("NotNull");
		}
		if (minAnnotation != null) {
			arrayList.add("Min(" + minAnnotation.value() + ")");
		}
		if (maxAnnotation != null) {
			arrayList.add("Max(" + maxAnnotation.value() + ")");
		}
		if (DigitsAnnotation != null) {
			arrayList.add(
					"Digits(integer=" + DigitsAnnotation.integer() + ", fraction=" + DigitsAnnotation.fraction() + ")");
		}
		if (sizeAnnotation != null) {
			arrayList.add("Size(min=" + sizeAnnotation.min() + ", max=" + sizeAnnotation.max() + ")");
		}
		if (pastAnnotation != null) {
			arrayList.add("Past");
		}
		if (futureAnnotation != null) {
			arrayList.add("Future");
		}
		if (notBlankAnnotation != null) {
			arrayList.add("NotBlank");
		}
		if (lengthAnnotation != null) {
			arrayList.add("Length(min=" + lengthAnnotation.min() + ", max=" + lengthAnnotation.max() + ")");
		}
		if (notEmptyAnnotation != null) {
			arrayList.add("NotEmpty");
		}
		if (rangeAnnotation != null) {
			arrayList.add("Range(min=" + rangeAnnotation.min() + ", max=" + rangeAnnotation.max() + ")");
		}
		if (emailAnnotation != null) {
			arrayList.add("Email");
		}
		if (patternAnnotation != null) {
			arrayList.add("Pattern(regex=" + patternAnnotation.regexp() + ")");
		}
		return arrayList;
	}

	public static class FullResource {
		private String title;
		private List<MethodDescription> methods;
		private HashMap<String, Object> postStructure = new HashMap();
		private String postStructureString;
		private ArrayList<ResourceUrl> relativeResource = new ArrayList();
		private ResourceUrl resource;
		private HashMap<String, List<Interceptor>> interceptors = new HashMap();
		private List<Object> fields = new ArrayList<Object>();
		private HashSet<EntityEvent> events = new HashSet();

		public HashSet<EntityEvent> getEvents() {
			return events;
		}

		public void setEvents(HashSet<EntityEvent> events) {
			this.events = events;
		}

		public String getPostStructureString() {
			return postStructureString;
		}

		public void setPostStructureString(String postStructureString) {
			this.postStructureString = postStructureString;
		}

		public List<MethodDescription> getMethods() {
			return methods;
		}

		public void setMethods(List<MethodDescription> methods) {
			this.methods = methods;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public HashMap<String, Object> getPostStructure() {
			return postStructure;
		}

		public void setPostStructure(HashMap<String, Object> postStructure) {
			this.postStructure = postStructure;
		}

	
		public List<Object> getFields() {
			return fields;
		}

		public void setFields(List<Object> fields) {
			this.fields = fields;
		}

		public HashMap<String, List<Interceptor>> getInterceptors() {
			return interceptors;
		}

		public void setInterceptors(HashMap<String, List<Interceptor>> interceptors) {
			this.interceptors = interceptors;
		}

		public ArrayList<ResourceUrl> getRelativeResource() {
			return relativeResource;
		}

		public void setRelativeResource(ArrayList<ResourceUrl> relativeResource) {
			this.relativeResource = relativeResource;
		}

		public ResourceUrl getResource() {
			return resource;
		}

		public void setResource(ResourceUrl resource) {
			this.resource = resource;
		}

	}

	public static class MethodDescription {
		String method;
		String description;

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}

	public static class ResourceUrl {
		private String resourceUri;
		private String structureUri;

		public String getResourceUri() {
			return resourceUri;
		}

		public void setResourceUri(String resourceUri) {
			this.resourceUri = resourceUri;
		}

		public String getStructureUri() {
			return structureUri;
		}

		public void setStructureUri(String structureUri) {
			this.structureUri = structureUri;
		}

	}

	public static class ObjectProperty {
		private String lifeCycle;
		private String dataType;
		private String resourceUri;
		private String structureUri;
		private String name;
		private String relationship;
		private ArrayList<String> constraints = new ArrayList();
		private int order;
		private String label;
		private String tip;

		public int getOrder() {
			return order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getTip() {
			return tip;
		}

		public void setTip(String tip) {
			this.tip = tip;
		}

		public String getStructureUri() {
			return structureUri;
		}

		public void setStructureUri(String structureUri) {
			this.structureUri = structureUri;
		}

		public String getLifeCycle() {
			return lifeCycle;
		}

		public void setLifeCycle(String lifeCycle) {
			this.lifeCycle = lifeCycle;
		}

		public String getDataType() {
			return dataType;
		}

		public void setDataType(String dataType) {
			this.dataType = dataType;
		}

		public String getResourceUri() {
			return resourceUri;
		}

		public void setResourceUri(String resourceUri) {
			this.resourceUri = resourceUri;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getRelationship() {
			return relationship;
		}

		public void setRelationship(String relationship) {
			this.relationship = relationship;
		}

		public ArrayList<String> getConstraints() {
			return constraints;
		}

		public void setConstraints(ArrayList<String> constraints) {
			this.constraints = constraints;
		}

	}

	public enum LifeCycle {
		PERSISTENT("持久化"), TRANSIENT("暂存");
		private String desc;

		LifeCycle(String desc) {
			this.desc = desc;
		}

		public String getDesc() {
			return desc;
		}

	}

	public static class Interceptor {
		private String name;
		private String description;
		private String path;

		private int order;

		public Interceptor(String name, String description, String path, int order) {
			super();
			this.name = name;
			this.description = description;
			this.path = path;
			this.order = order;
		}

		public int getOrder() {
			return order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

	}

	public static class SimpleResource {
		private String name;
		private String resourceUri;
		private String structureUri;

		public String getStructureUri() {
			return structureUri;
		}

		public void setStructureUri(String structureUri) {
			this.structureUri = structureUri;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getResourceUri() {
			return resourceUri;
		}

		public void setResourceUri(String resourceUri) {
			this.resourceUri = resourceUri;
		}

	}
}
