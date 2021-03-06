package com.xjcy.struts.web;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import com.xjcy.struts.StrutsFilter;

public class StrutsServletContainerInitializer implements ServletContainerInitializer
{

	private static final Logger logger = Logger.getLogger(StrutsServletContainerInitializer.class);

	@Override
	public void onStartup(Set<Class<?>> arg0, ServletContext arg1) throws ServletException
	{
		// 添加context监听
		arg1.addListener(ContextLoaderListener.class);
		// 增加filter
		arg1.setAttribute("StrutsFilter", arg1.addFilter("StrutsFilter", StrutsFilter.class));

		if (logger.isDebugEnabled())
			logger.debug("Container startup with ContextLoaderListener, StrutsFilter");
	}
}
