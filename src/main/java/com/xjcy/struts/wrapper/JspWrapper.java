package com.xjcy.struts.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.JspC;
import org.apache.jasper.compiler.JspUtil;
import org.apache.jasper.runtime.HttpJspBase;
import org.apache.log4j.Logger;

import com.xjcy.struts.context.StrutsContext;
import com.xjcy.struts.context.WebContextUtils;

public class JspWrapper
{
	private static final Logger logger = Logger.getLogger(JspWrapper.class);

	private static final Map<String, HttpJspBase> jspServlets = new HashMap<>();
	private static final Map<String, Long> jspLastTimes = new HashMap<>();

	public void processJsp(String jspUri, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// 线上环境执行预编译的jspServlet
		if (WebContextUtils.isLinuxOS())
		{
			ServletContext context = request.getServletContext();
			File jspFile = new File(context.getRealPath(jspUri));
			if (!jspFile.exists())
				throw new IOException("Not found the jsp file " + jspUri);
			Long lastModified = jspLastTimes.get(jspUri);
			HttpJspBase jspBase;
			if (jspServlets.containsKey(jspUri) && lastModified == jspFile.lastModified())
				jspBase = jspServlets.get(jspUri);
			else
			{
				// 判断已经加载过，重新编译jsp文件
				if (lastModified != null && del(jspUri, context))
				{
					if (reCompiler(jspUri, context))
						jspBase = getServlet(context, jspUri);
					else
						throw new IOException("Rebuild jsp servlet " + jspUri + " faild");
				}
				else
					jspBase = getServlet(context, jspUri);
				if (jspBase == null)
					throw new IOException("Not found jsp servlet " + jspUri);
				jspServlets.put(jspUri, jspBase);
			}
			jspBase.init(new JspServletConfig(context));
			jspBase.service(request, response);
			jspBase.destroy();
		}
		else
		{
			// 执行跳转
			request.getRequestDispatcher(jspUri).forward(request, response);
		}
	}

	private boolean del(String jspUri, ServletContext context)
	{
		int iSep = jspUri.lastIndexOf('/') + 1;
		String className = JspUtil.makeJavaIdentifier(jspUri.substring(iSep));
		File file1 = WebContextUtils.getJspServletFile(context, className + ".class");
		File file2 = WebContextUtils.getJspServletFile(context, className + ".java");
		return file1.delete() && file2.delete();
	}

	private boolean reCompiler(String jspUri, ServletContext context)
	{
		try
		{
			JspC jspc = new JspC()
			{
				@Override
				public String getCompilerClassName()
				{
					// 设置编译器为JDTCompiler
					return "org.apache.jasper.compiler.JDTCompiler";
				}
			};

			jspc.setTrimSpaces(true);// 去除\t
			jspc.setJavaEncoding("utf-8");
			jspc.setGenStringAsCharArray(true); // 文本字符串生成数组来提高性能

			jspc.setUriroot(context.getRealPath("/"));// web应用的root目录
			jspc.setOutputDir(context.getRealPath(StrutsContext.CLASS_PATH));// .java文件和.class文件的输出目录
			jspc.setJspFiles(jspUri.substring(1, jspUri.length()));

			jspc.setCompile(true);// 是否编译 false或不指定的话只生成.java文件
			long start = System.currentTimeMillis();
			jspc.execute();
			logger.debug("Jsp " + jspUri + " rebuild success in " + (System.currentTimeMillis() - start) + " ms");
			return true;
		}
		catch (Exception e)
		{
			logger.error("编译jsp文件失败", e);
		}
		return false;
	}

	private static HttpJspBase getServlet(ServletContext context, String jspUri) throws IOException
	{
		try
		{
			int iSep = jspUri.lastIndexOf('/') + 1;
			String className = JspUtil.makeJavaIdentifier(jspUri.substring(iSep));
			File servletFile = WebContextUtils.getJspServletFile(context, className + ".class");
			JspClassLoader loader = new JspClassLoader(servletFile, Thread.currentThread().getContextClassLoader());
			Class<?> cla = loader.loadClass("org.apache.jsp." + className);
			loader.close();
			// 缓存编译的jsp文件最后修改时间
			jspLastTimes.put(jspUri, servletFile.lastModified());
			return (HttpJspBase) cla.newInstance();
		}
		catch (IllegalAccessException | ClassNotFoundException | InstantiationException e)
		{
			logger.error("获取jspServlet失败", e);
			return null;
		}
	}

	public final class JspServletConfig implements ServletConfig
	{

		private ServletContext context;

		public JspServletConfig(ServletContext context)
		{
			this.context = context;
		}

		@Override
		public String getInitParameter(String arg0)
		{
			return null;
		}

		@Override
		public Enumeration<String> getInitParameterNames()
		{
			return null;
		}

		@Override
		public ServletContext getServletContext()
		{
			return this.context;
		}

		@Override
		public String getServletName()
		{
			return "struts_jsp";
		}
	}
}
