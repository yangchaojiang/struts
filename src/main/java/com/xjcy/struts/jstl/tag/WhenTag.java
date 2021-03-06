package com.xjcy.struts.jstl.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;

public class WhenTag extends BodyTagSupport
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4778987987915422993L;
	private static final Logger logger = Logger.getLogger(WhenTag.class);

	private String test;

	public void setTest(String test)
	{
		this.test = test;
	}

	@Override
	public int doStartTag() throws JspException
	{
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws JspException
	{
		CaseTag parent = (CaseTag) this.getParent();
		if (Boolean.parseBoolean(test) && parent.isOk() == false)
		{
			try
			{
				// 利用getString方法得到字符串
				String content = this.getBodyContent().getString();
				// 输出到浏览器
				this.pageContext.getOut().append(content.trim());
				parent.setOk(true);
			}
			catch (IOException e)
			{
				logger.error("输出if标签失败", e);
			}
		}
		return EVAL_PAGE;
	}
}
