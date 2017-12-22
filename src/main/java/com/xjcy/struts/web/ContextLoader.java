package com.xjcy.struts.web;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.xjcy.struts.context.StrutsContext;
import com.xjcy.struts.context.WebContextUtils;
import com.xjcy.util.StringUtils;

public class ContextLoader
{

	private static final Logger logger = Logger.getLogger(ContextLoader.class);
	private final static StrutsContext context = new StrutsContext();
	private static String classPath;
	private ServletContext severtContext;

	public ContextLoader(ServletContext servletContext)
	{
		long start = System.nanoTime();
		try
		{
			this.severtContext = servletContext;
			// 扫描所有文件
			loadContext(servletContext);
		}
		catch (Exception e)
		{
			logger.error("Context load faild", e);
		}
		if (logger.isDebugEnabled())
		{
			logger.debug("Scan to " + context.getClassSize() + " class files");
			logger.debug("Struts context load with " + (System.nanoTime() - start) + "ns");
		}
	}

	private static void loadContext(ServletContext arg1)
	{
		context.clear();
		classPath = arg1.getRealPath(StrutsContext.CLASS_PATH);
		try
		{
			scanFiles(classPath);
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
		{
			logger.error("Scan class failed", e);
		}
	}

	private static void scanFiles(String rootPath)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		File dir = new File(rootPath);
		// 该文件目录下文件全部放入数组
		File[] files = dir.listFiles();
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				// 判断是文件还是文件夹
				if (files[i].isDirectory())
					scanFiles(files[i].getAbsolutePath()); // 获取文件绝对路径
				else
				{
					if (files[i].getName().endsWith(".class"))
					{
						Class<?> cla = getClass(files[i]);
						if (!cla.isInterface())
						{
							bindResource(cla);
							// 如果继承了ActionSupport，则添加到集合中去
							if (WebContextUtils.isAction(cla))
								bindAction(cla);
							else if (WebContextUtils.isInterceptor(cla))
								context.addInterceptor(cla);
							else if (WebContextUtils.isStrutsInit(cla))
								context.addInit(cla);
							// 将class放入集合
							context.addClass(cla);
						}
					}
				}
			}
		}
	}

	private static Class<?> getClass(File file) throws ClassNotFoundException
	{
		return Class.forName(file.getPath().replace(classPath, "").replace(File.separator, ".").replace(".class", ""));
	}

	private static void bindResource(Class<?> cla)
	{
		Field[] fields = cla.getDeclaredFields();
		for (Field field : fields)
		{
			if (field.getAnnotation(Resource.class) != null)
			{
				context.addResource(field);
			}
		}
	}

	private static void bindAction(Class<?> cla)
	{
		String pkg = WebContextUtils.getMappingPath(cla);
		Method[] methods = cla.getMethods();
		String action;
		for (Method method : methods)
		{
			// 获取request路径
			action = WebContextUtils.getMappingPath(pkg, method);
			if (!StringUtils.isEmpty(action))
			{
				if (action.contains("{") && action.contains("}"))
				{
					List<String> paras = new ArrayList<>();
					int start = 0;
					String para;
					String pattern = action;
					while (true)
					{
						para = getParameter(action, start);
						if(para == null)
							break;
						start += para.length();
						paras.add(para.replace("{", "").replace("}", ""));
						pattern = pattern.replace(para, "(.*)");
					}
					context.addAction(pattern, method, cla, paras);
				}
				else
					context.addAction(action, method, cla);
			}
		}
	}

	private static String getParameter(String action, int start)
	{
		int begin = action.indexOf("{", start);
		if (begin == -1)
			return null;
		int end = action.indexOf("}", begin) + 1;
		return action.substring(begin, end);
	}

	public StrutsContext getContext()
	{
		return context;
	}

	public void destroy()
	{
		if (context != null)
			context.destory();
	}

	public void startup()
	{
		if (context != null)
			context.startup(severtContext);
	}
}
