package com.xjcy.struts.context;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterRegistration.Dynamic;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.JspC;
import org.apache.log4j.Logger;

import com.xjcy.struts.ActionInterceptor;
import com.xjcy.struts.StrutsInit;
import com.xjcy.struts.annotation.Order;
import com.xjcy.struts.mapper.ActionMapper;
import com.xjcy.struts.mapper.SpringBean;

public class StrutsContext
{
	private static final Logger logger = Logger.getLogger(StrutsContext.class);

	public static final String CLASS_PATH = "WEB-INF/classes/";
	private static final List<Class<?>> classlist = new ArrayList<>();
	private static final List<Class<?>> initList = new ArrayList<>();
	private static final List<StrutsInit> startedInit = new ArrayList<>();
	private static final List<Field> resourceList = new ArrayList<>();
	private static final List<Class<?>> interceptors = new ArrayList<>();
	private static final Map<String, ActionMapper> actionMap = new HashMap<>();
	private static final Map<String, ActionMapper> patternActionMap = new HashMap<>();
	private static final Map<Field, SpringBean> springMap = new HashMap<>();

	public void startup(ServletContext sc)
	{
		if (actionMap.size() > 0)
		{
			mappingAction(sc);
		}
		if (resourceList.size() > 0)
		{
			findResource();
		}
		if (interceptors.size() > 1)
		{
			sortInterceptor();
		}
		if (initList.size() > 0)
		{
			startInit(sc);
		}
		// 判断线上环境，执行预编译
		if (WebContextUtils.isLinuxOS())
			recompire(sc);
	}

	private void recompire(ServletContext sc)
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

			jspc.setUriroot(sc.getRealPath("/"));// web应用的root目录

			String outDir = sc.getRealPath(CLASS_PATH);

			try
			{
				File output = new File(outDir + "/org");
				if (output.exists())
				{
					deleteDir(output);
					logger.debug("Clear all last build files");
				}
			}
			catch (Exception e)
			{
				logger.error("删除output目录失败", e);
			}
			jspc.setOutputDir(outDir);// .java文件和.class文件的输出目录
			logger.debug("Jsp output dir =" + outDir);

			jspc.setCompile(true);// 是否编译 false或不指定的话只生成.java文件
			long start = System.currentTimeMillis();
			jspc.execute();
			logger.debug("Jsp servlet build success in " + (System.currentTimeMillis() - start) + " ms");
		}
		catch (Exception e)
		{
			logger.error("编译jsp文件失败", e);
		}
	}

	private static boolean deleteDir(File dir)
	{
		if (dir.isDirectory())
		{
			String[] children = dir.list();
			// 递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++)
			{
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success)
				{
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	private void startInit(ServletContext sc)
	{
		try
		{
			StrutsInit init;
			for (Class<?> cla : initList)
			{
				init = (StrutsInit) cla.newInstance();
				init.init(sc);
				startedInit.add(init);
			}
		}
		catch (Exception e)
		{
			logger.error("Struts init faild", e);
		}
	}

	private void sortInterceptor()
	{
		// 按Order注解排序
		Collections.sort(interceptors, new Comparator<Class<?>>()
		{
			@Override
			public int compare(Class<?> arg0, Class<?> arg1)
			{
				Integer o1 = 0, o2 = 0;
				Order order0 = arg0.getAnnotation(Order.class);
				if (order0 != null)
					o1 = order0.value();
				Order order1 = arg1.getAnnotation(Order.class);
				if (order1 != null)
					o2 = order1.value();
				return o1.compareTo(o2);
			}
		});
	}

	private void findResource()
	{
		for (Field field : resourceList)
		{
			for (Class<?> cla : classlist)
			{
				if (field.getType().isAssignableFrom(cla))
				{
					springMap.put(field, new SpringBean(cla, field));
					break;
				}
			}
		}
	}

	private void mappingAction(ServletContext sc)
	{
		Dynamic filter = (Dynamic) sc.getAttribute("StrutsFilter");
		// 如果没有通配符的链接
		if (patternActionMap.isEmpty())
		{
			Set<String> actions = actionMap.keySet();
			for (String action : actions)
			{
				filter.addMappingForUrlPatterns(null, true, action);
			}
		}
		else
		{
			filter.addMappingForUrlPatterns(null, true, "/*");
		}
	}

	public void clear()
	{
		classlist.clear();
		initList.clear();
		resourceList.clear();
		interceptors.clear();
		actionMap.clear();
	}

	public void destory()
	{
		clear();
		if (startedInit.size() > 0)
		{
			destroyInit();
		}
	}

	private void destroyInit()
	{
		try
		{
			for (StrutsInit init : startedInit)
			{
				init.destroy();
			}
		}
		catch (Exception e)
		{
			logger.error("Struts destroy faild", e);
		}
	}

	public void addInterceptor(Class<?> cla)
	{
		interceptors.add(cla);
	}

	public void addInit(Class<?> cla)
	{
		initList.add(cla);
	}

	public void addClass(Class<?> cla)
	{
		classlist.add(cla);
	}

	public void addResource(Field field)
	{
		resourceList.add(field);
	}

	public void addAction(String action, Method method, Class<?> cla)
	{
		ActionMapper actionMapper = actionMap.put(action, new ActionMapper(method, cla));
		if (actionMapper != null)
			logger.error("Action " + action + " exist, override with " + method.getName());
	}

	public void addAction(String pattern, Method method, Class<?> cla, List<String> paras)
	{
		ActionMapper actionMapper = patternActionMap.put(pattern, new ActionMapper(method, cla, paras));
		if (actionMapper != null)
			logger.error("Action pattern " + pattern + " exist, override with " + method.getName());
	}

	public int getClassSize()
	{
		return classlist.size();
	}

	public ActionMapper getAction(String servletPath)
	{
		ActionMapper mapper = actionMap.get(servletPath);
		if (mapper == null)
		{
			Set<String> patterns = patternActionMap.keySet();
			for (String pattern : patterns)
			{
				Matcher match = Pattern.compile(pattern).matcher(servletPath);
				while (match.find())
				{
					mapper = patternActionMap.get(pattern);
					mapper.setParasValue(match);
					break;
				}
			}
		}
		return mapper;
	}

	public boolean checkInterceptors(HttpServletRequest request, HttpServletResponse response)
	{
		if (interceptors.isEmpty())
			return true;
		ActionInterceptor interceptor;
		for (Class<?> cla : interceptors)
		{
			interceptor = (ActionInterceptor) getBean(cla);
			logger.debug("Check " + cla.getSimpleName());
			if (!interceptor.intercept(request, response))
				return false;
		}
		return true;
	}

	public Object getBean(Class<?> controller)
	{
		try
		{
			Object obj = controller.newInstance();
			annotationInject(obj);
			if (logger.isDebugEnabled())
				logger.debug("Finished creating instance of bean '" + controller.getSimpleName() + "'");
			return obj;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			logger.error("Create '" + controller.getName() + "' bean faild", e);
		}
		return null;
	}

	private void annotationInject(Object obj)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException
	{
		if (obj == null)
			return;
		Field[] fields = obj.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			if (field.getAnnotation(Resource.class) != null && findBean(field))
			{
				field.setAccessible(true);
				field.set(obj, getBean(springMap.get(field).getBeanClass()));
				field.setAccessible(false);
			}
		}
	}

	private static boolean findBean(Field key)
	{
		return springMap.containsKey(key);
	}

	public int actionSize()
	{
		return actionMap.size() + patternActionMap.size();
	}

	public int beanSize()
	{
		return springMap.size();
	}

	public int interceptorSize()
	{
		return interceptors.size();
	}

}
