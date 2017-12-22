package com.xjcy.struts;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.xjcy.struts.context.StrutsContext;
import com.xjcy.struts.context.WebContextUtils;
import com.xjcy.struts.mapper.ActionMapper;
import com.xjcy.struts.wrapper.ResponseWrapper;
import com.xjcy.util.StringUtils;

public class StrutsFilter implements Filter
{
	private static final Logger logger = Logger.getLogger(StrutsFilter.class);
	private static final String CHARSET_UTF8 = "utf-8";
	private static String basePath;
	private static String serverName;
	private StrutsContext context;
	private static final ResponseWrapper responseWrapper = new ResponseWrapper();

	@Override
	public void destroy()
	{
		context.clear();
		context = null;
		if (logger.isDebugEnabled())
			logger.debug("destroy context");
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException
	{
		// 设置编码
		arg0.setCharacterEncoding(CHARSET_UTF8);
		arg1.setCharacterEncoding(CHARSET_UTF8);
		HttpServletRequest request = (HttpServletRequest) arg0;
		String servletPath = request.getServletPath();
		if (logger.isDebugEnabled())
			logger.debug("Filter => " + servletPath);
		ActionMapper action = this.context.getAction(servletPath);
		if (action == null)
		{
			logger.error("The action of '" + servletPath + "' not found");
			arg2.doFilter(arg0, arg1);
		}
		else
		{
			// 添加主目录属性
			request.setAttribute("basePath", getBasePath(request));
			if (action.isPatternAction())
			{
				Map<String, String> map = action.getParaValues();
				Set<Entry<String, String>> entries = map.entrySet();
				for (Entry<String, String> entry : entries)
				{
					request.setAttribute(entry.getKey(), entry.getValue());
					logger.debug("赋值PatternAction：" + entry.getKey() + "=" + entry.getValue());
				}
			}
			HttpServletResponse response = (HttpServletResponse) arg1;
			Object resultObj;
			try
			{
				if (!context.checkInterceptors(request, response))
				{
					if (logger.isDebugEnabled())
						logger.debug("Action is blocked by Interceptor");
					return;
				}
				logger.debug("Find the action => " + action.toString());
				ActionSupport as = (ActionSupport) context.getBean(action.getController());
				resultObj = action.invoke(as, request, response);
				if (resultObj != null)
				{
					responseWrapper.setReturnObj(action.getReturnType(), resultObj);
					responseWrapper.doResponse(request, response);
				}
			}
			catch (IllegalArgumentException e)
			{
				logger.error("执行 action '" + action.getController().getName() + "' 失败", e);
			}
		}
	}

	private static Object getBasePath(HttpServletRequest request)
	{
		String server = request.getServerName();
		if (StringUtils.isEmpty(basePath) || !server.equals(serverName))
		{
			serverName = server;
			String path = "/ROOT".equals(request.getContextPath()) ? "" : request.getContextPath();
			String port = (request.getServerPort() == 80 ? "" : ":" + request.getServerPort());
			basePath = request.getScheme() + "://" + server + port + path + "/";
		}
		return basePath;
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException
	{
		context = WebContextUtils.getWebApplicationContext(arg0.getServletContext());
		if (logger.isDebugEnabled())
			logger.debug("Find actions " + context.actionSize() + " beans " + context.beanSize() + " interceptors "
					+ context.interceptorSize());
	}

}
