package com.xjcy.struts.jstl.tag;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class ForeachTag extends BodyTagSupport
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3853464038808887054L;

	private String var;
	private Iterator<?> iterator;
	private String index;
	private int num = 0;

	public void setVar(String var)
	{
		this.var = var;
	}

	public void setItems(Object items)
	{
		this.num = 0;
		this.iterator = null;
		if(items == null)
		{
			
		}
		else if (items instanceof Map)
		{
			Map<?, ?> map = (Map<?, ?>) items;
			this.iterator = map.entrySet().iterator();
		}
		else if (items instanceof Collection)
		{
			Collection<?> c = (Collection<?>) items;
			this.iterator = c.iterator();
		}
		else
			throw new IllegalStateException("Not supported.");
	}

	public void setIndex(String index)
	{
		this.index = index;
	}

	/**
	 * EVAL_BODY_INCLUDE：告诉服务器正文的内容，并把这些内容送入输出流 
	 * SKIP_BODY：告诉服务器不要处理正文内容
	 * EVAL_PAGE：让服务器继续执行页面 
	 * SKIP_PAGE：让服务器不要处理剩余的页面
	 * EVAL_BODY_AGAIN：让服务器继续处理正文内容，只有doAfterBody方法可以返回
	 * EVAL_BODY_BUFFERED：BodyTag接口的字段，在doStartTag（）返回
	 * EVAL_BODY_INCLUDE、SKIP_BODY一般由doStartTag（）返回，而EVAL_PAPGE、
	 * SKIP_PAGE由doEndTag（）返回。
	 */
	@Override
	public int doStartTag() throws JspException
	{
		if (this.process())
			return EVAL_BODY_INCLUDE;
		return EVAL_PAGE;
	}

	private boolean process()
	{
		if (null != iterator && iterator.hasNext())
		{
			pageContext.setAttribute(var, iterator.next());
			// 存在索引变量，记录索引
			if (index != null)
			{
				pageContext.setAttribute(index, num);
				num++;
			}
			return true;
		}
		return false;
	}

	@Override
	public int doAfterBody() throws JspException
	{
		if (this.process())
			return EVAL_BODY_AGAIN;
		return EVAL_PAGE;
	}
}
