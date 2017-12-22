package com.xjcy.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ActionInterceptor
{
	public abstract boolean intercept(HttpServletRequest arg0, HttpServletResponse arg1);
}
