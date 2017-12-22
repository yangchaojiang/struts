package com.xjcy.struts.mapper;

import java.lang.reflect.Field;

public class SpringBean
{
	private Class<?> beanClass;
	private Field beanField;

	public SpringBean(Class<?> cla, Field field)
	{
		this.beanField = field;
		this.beanClass = cla;
	}

	public Class<?> getBeanClass()
	{
		return this.beanClass;
	}

	public Field getBeanField()
	{
		return this.beanField;
	}

}
