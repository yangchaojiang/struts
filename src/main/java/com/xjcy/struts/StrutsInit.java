package com.xjcy.struts;

import javax.servlet.ServletContext;

public abstract class StrutsInit
{
	public abstract void init(ServletContext servletContext);
	
	public abstract void destroy();
}
