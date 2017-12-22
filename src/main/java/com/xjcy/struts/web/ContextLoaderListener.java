package com.xjcy.struts.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.xjcy.struts.context.WebContextUtils;

public class ContextLoaderListener implements ServletContextListener
{
	private static final Logger logger = Logger.getLogger(ContextLoaderListener.class);

	private ContextLoader contextLoader;

	@Override
	public void contextDestroyed(ServletContextEvent arg0)
	{
		if (contextLoader != null)
			this.contextLoader.destroy();
		ServletContext servletContext = arg0.getServletContext();
		servletContext.removeAttribute(WebContextUtils.STRUTS2_IS_LOAD);
		servletContext.removeAttribute(WebContextUtils.STRUTS2_CONTEXT);
		if (logger.isDebugEnabled())
			logger.debug("Struts context closed");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0)
	{
		ServletContext servletContext = arg0.getServletContext();
		this.contextLoader = new ContextLoader(servletContext);
		this.contextLoader.startup();
		servletContext.setAttribute(WebContextUtils.STRUTS2_IS_LOAD, true);
		servletContext.setAttribute(WebContextUtils.STRUTS2_CONTEXT, this.contextLoader.getContext());
		if (logger.isDebugEnabled())
			logger.debug("Struts context started");
	}

}
