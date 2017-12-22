package com.xjcy.struts.jstl.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;

public class RoundTag extends BodyTagSupport
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(RoundTag.class);

	private String value;

	public void setValue(String value)
	{
		this.value = value;
	}

	private int digits = 2;

	public void setDigits(int digits)
	{
		this.digits = digits;
	}

	@Override
	public int doStartTag() throws JspException
	{
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws JspException
	{
		String format = "#";
		if (digits > 0)
		{
			format += ".";
			for (int i = 0; i < digits; i++)
			{
				format += "0";
			}
		}
		try
		{
			double d = 0.00D;
			try
			{
				d = Double.parseDouble(value);
			}
			catch (Exception e)
			{
				logger.error("输出round标签失败", e);
			}
			// 输出到浏览器
			this.pageContext.getOut().append(new java.text.DecimalFormat(format).format(d));
		}
		catch (IOException e)
		{
			logger.error("输出round标签失败", e);
		}
		return EVAL_PAGE;
	}
}
