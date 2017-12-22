package com.xjcy.struts.mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.xjcy.struts.ActionSupport;

/**
 * Action处理类
 * 
 * @author YYDF
 *
 */
public class ActionMapper
{
	private static final Logger logger = Logger.getLogger(ActionMapper.class);

	private final Method actionMethod;
	private final Class<?> controller;
	private final Class<?> returnType;
	private final List<String> paras;
	private final Map<String, String> paraValues = new HashMap<>();

	public ActionMapper(Method method, Class<?> cla)
	{
		this(method, cla, null);
	}

	public ActionMapper(Method method, Class<?> cla, List<String> paras)
	{
		this.actionMethod = method;
		this.controller = cla;
		this.returnType = method.getReturnType();
		this.paras = paras;
	}

	public Object invoke(ActionSupport as, HttpServletRequest request, HttpServletResponse response)
	{
		Object resultObj = null;
		try
		{
			as.setRequest(request);
			as.setResponse(response);
			resultObj = actionMethod.invoke(as);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			logger.error("Action call " + actionMethod.getName() + " faild", e);
		}
		return resultObj;
	}

	@Override
	public String toString()
	{
		return "[method=" + actionMethod.getName() + ", controller=" + controller.getSimpleName() + ", return="
				+ returnType.getSimpleName() + "]";
	}

	public Class<?> getController()
	{
		return this.controller;
	}

	public boolean isPatternAction()
	{
		return this.paras != null;
	}

	public void setParasValue(Matcher match)
	{
		paraValues.clear();
		int num = 1;
		for (String para : paras)
		{
			paraValues.put(para, match.group(num));
			num++;
		}
	}

	public Class<?> getReturnType()
	{
		return this.returnType;
	}

	public void fillRequest(HttpServletRequest request)
	{
		Set<String> keys = paraValues.keySet();
		for (String key : keys)
		{
			request.setAttribute(key, paraValues.get(key));
		}
	}
}
