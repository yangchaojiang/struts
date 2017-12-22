package com.xjcy.struts.mapper;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.xjcy.struts.wrapper.EachItem;
import com.xjcy.util.StringUtils;

/**
 * jsp的对象和跳转页面配置
 * 
 * @author 张梦龙
 *
 */
public class ModelAndView
{
	private static final Logger logger = Logger.getLogger(ModelAndView.class);

	private final Map<String, Object> paras = new HashMap<>();
	private String viewName;

	public ModelAndView addObject(String key, Object obj)
	{
		paras.put(key, obj);
		return this;
	}

	public ModelAndView addObject(String key, Object obj, String sizePara)
	{
		paras.put(key, obj);
		// 如果size的参数不为空
		if (!StringUtils.isEmpty(sizePara))
		{
			if (obj == null)
				addObject(sizePara, 0);
			else
			{
				if (obj instanceof Map)
					addObject(sizePara, ((Map<?, ?>) obj).size());
				else if (obj instanceof Collection)
					addObject(sizePara, ((Collection<?>) obj).size());
				else
					logger.debug("Unknown object => " + obj.getClass());
			}
		}
		return this;
	}

	public <T> ModelAndView addObject(String key, EachItem<T> each)
	{
		List<T> objs = each.getList();
		if (objs == null)
			return this;
		for (T t : objs)
		{
			each.doItem(t);
		}
		paras.put(key, objs);
		return this;
	}

	public ModelAndView addObject(Object obj)
	{
		if (obj == null)
			return this;
		Field[] fields = obj.getClass().getDeclaredFields();
		if (fields.length > 0)
		{
			for (Field field : fields)
			{
				if ("serialVersionUID".equals(field.getName()))
					continue;
				try
				{
					field.setAccessible(true);
					addObject(field.getName(), field.get(obj));
					if (logger.isDebugEnabled())
						logger.debug("addObject => " + field.getName() + "[" + field.get(obj) + "]");
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					logger.error("addObject失败", e);
				}
			}
		}
		return this;
	}

	public String getViewName()
	{
		// 不是以jsp结尾，也没有后缀名
		if (!viewName.endsWith(".jsp") && !viewName.contains("."))
			viewName += ".jsp";
		// 前面加/
		if (!viewName.startsWith("/"))
			viewName = "/" + viewName;
		return viewName;
	}

	public Map<String, Object> getParas()
	{
		return paras;
	}

	public ModelAndView removeObject(String key)
	{
		if (paras.containsKey(key))
			paras.remove(key);
		return this;
	}

	public ModelAndView setViewName(String viewName)
	{
		this.viewName = viewName;
		return this;
	}

	public void clear()
	{
		if (paras != null)
			paras.clear();
		viewName = null;
	}

	public void fillRequest(HttpServletRequest request)
	{
		if (paras != null && !paras.isEmpty())
		{
			Set<String> keys = paras.keySet();
			for (String key : keys)
			{
				request.setAttribute(key, paras.get(key));
			}
		}
	}

}
