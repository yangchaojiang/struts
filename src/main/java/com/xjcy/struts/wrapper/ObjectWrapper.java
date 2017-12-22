package com.xjcy.struts.wrapper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class ObjectWrapper
{
	private static final Logger logger = Logger.getLogger(ObjectWrapper.class);

	static final String STR_EMPTY = "";
	static final String STR_VERSION_UID = "serialVersionUID";
	static final Map<String, Field[]> cacheFields = new HashMap<>();
	static final StringBuilder json = new StringBuilder();

	public String write(Map<String, Object> jsonMap)
	{
		long start = System.nanoTime();
		// 清空json
		json.setLength(0);
		try
		{
			appendMap(null, jsonMap);
			logger.debug("JSON build in " + (System.nanoTime() - start) + "ns");
		}
		catch (Exception e)
		{
			logger.error("转换JSON失败", e);
		}
		return json.toString();
	}

	private static void appendObj(String key, Object value)
	{
		if (value == null || STR_VERSION_UID.equals(key) || value instanceof Logger)
		{
			// do noting
		}
		else if (value instanceof String || value instanceof CharSequence)
			json.append("\"").append(key).append("\":\"").append(value).append("\",");
		else if (value instanceof Integer || value instanceof Boolean || value instanceof Long)
			json.append("\"").append(key).append("\":").append(value).append(",");
		else if (value instanceof Map)
			appendMap(key, (Map<?, ?>) value);
		else if (value instanceof Object[] 
				|| value instanceof int[] 
				|| value instanceof long[]
				|| value instanceof char[])
			appendArray(key, value);
		else if (value instanceof Collection)
			appendList(key, (Collection<?>) value);
		else
			// 解析bean
			appendBean(key, value);
	}

	private static void appendBean(String key, Object value)
	{
		json.append("\"").append(key).append("\":{");
		Field[] fields = getDeclaredFields(value);
		int num = 0;
		Object obj2;
		for (Field field : fields)
		{
			obj2 = getObject(field, value);
			if (obj2 != null)
			{
				appendObj(field.getName(), obj2);
				num++;
			}
		}
		if (num > 0)
			json.delete(json.length() - 1, json.length());
		json.append("},");
	}

	/**
	 * 处理Map
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private static void appendMap(String key, Map<?, ?> value)
	{
		if (key == null)
		{
			json.append("{");
			Set<?> keys = value.keySet();
			for (Object obj : keys)
			{
				appendObj(obj.toString(), value.get(obj));
			}
			if (!value.isEmpty())
				json.delete(json.length() - 1, json.length());
			json.append("}");
		}
		else
		{
			json.append("\"").append(key).append("\":[");
			Set<?> keys = value.keySet();
			for (Object obj : keys)
			{
				json.append("{");
				appendObj(obj.toString(), value.get(obj));
				json.append("},");
			}
			if (!value.isEmpty())
				json.delete(json.length() - 1, json.length());
			json.append("],");
		}
	}

	/***
	 * 处理List集合
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private static void appendList(String key, Collection<?> value)
	{
		json.append("\"").append(key).append("\":[");
		Field[] fields;
		Object obj2;
		int num;
		for (Object obj : value)
		{
			json.append("{");
			fields = getDeclaredFields(obj);
			num = 0;
			for (Field field : fields)
			{
				obj2 = getObject(field, obj);
				if (obj2 != null)
				{
					appendObj(field.getName(), obj2);
					num++;
				}
			}
			if (num > 0)
				json.delete(json.length() - 1, json.length());
			json.append("},");
		}
		if (value.size() > 0)
			json.delete(json.length() - 1, json.length());
		json.append("],");
	}

	private static Object getObject(Field field, Object obj)
	{
		try
		{
			field.setAccessible(true);
			return field.get(obj);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			return null;
		}
		finally
		{
			field.setAccessible(false);
		}
	}

	private static synchronized Field[] getDeclaredFields(Object obj)
	{
		String cacheKey = obj.getClass().getName();
		if (cacheFields.containsKey(cacheKey))
			return cacheFields.get(cacheKey);
		Field[] fields = obj.getClass().getDeclaredFields();
		cacheFields.put(cacheKey, fields);
		return fields;
	}

	/**
	 * 处理数组
	 * 
	 * @param key
	 * @param array
	 * @return
	 */
	private static void appendArray(String key, Object array)
	{
		json.append("\"").append(key).append("\":[");
		int len = Array.getLength(array);
		Object obj;
		for (int i = 0; i < len; i++)
		{
			obj = Array.get(array, i);
			if (obj instanceof String)
				json.append("\"").append(obj).append("\"");
			else if (obj instanceof Integer || obj instanceof Long)
				json.append("\"").append(obj).append("\"");
			else
				json.append(obj);
			json.append(",");
		}
		if (len > 0)
			json.delete(json.length() - 1, json.length());
		json.append("],");
	}
}
