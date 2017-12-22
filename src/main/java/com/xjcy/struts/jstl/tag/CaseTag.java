package com.xjcy.struts.jstl.tag;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class CaseTag extends BodyTagSupport
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5738376830292291109L;
	
	private boolean isOk;

	@Override
	public int doStartTag() throws JspException
	{
		isOk = false;
		return EVAL_BODY_INCLUDE;
	}

	public boolean isOk()
	{
		return isOk;
	}

	public void setOk(boolean isOk)
	{
		this.isOk = isOk;
	}

}
