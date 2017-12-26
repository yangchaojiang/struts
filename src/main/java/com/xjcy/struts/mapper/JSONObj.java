package com.xjcy.struts.mapper;

import java.util.HashMap;
import java.util.Map;

import com.xjcy.struts.wrapper.ObjectWrapper;

/**
 * JSON对象处理类
 * 
 * @author YYDF
 *
 */
public class JSONObj
{
	final Map<String, Object> jsonMap = new HashMap<>();
	static final ObjectWrapper wrapper = new ObjectWrapper();
	private String json;
	
	public JSONObj()
	{
		this(null);
	}

	public JSONObj(String json)
	{
		this.json = json;
	}

	public JSONObj put(String key, Object val)
	{
		jsonMap.put(key, val);
		return this;
	}

	public JSONObj putAll(Map<String, Object> map)
	{
		jsonMap.putAll(map);
		return this;
	}

	public Map<String, Object> getMap()
	{
		return jsonMap;
	}

	public static JSONObj success()
	{
		JSONObj jsonObj = new JSONObj();
		jsonObj.put("success", true);
		jsonObj.put("errcode", 0);
		jsonObj.put("errmsg", "ok");
		return jsonObj;
	}

	public static JSONObj error(int errcode, String errmsg)
	{
		JSONObj jsonObj = new JSONObj();
		jsonObj.put("success", false);
		jsonObj.put("errcode", errcode);
		jsonObj.put("errmsg", errmsg);
		return jsonObj;
	}

	@Override
	public String toString()
	{
		if (json == null) 
			return wrapper.write(jsonMap);// 转换成JSON
		return json;
	}

}
