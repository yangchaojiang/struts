package com.xjcy.struts.wrapper;

import java.util.List;

public abstract class EachItem<T>
{
	private List<T> objs;

	public EachItem(List<T> objs)
	{
		this.objs = objs;
	}

	public abstract void doItem(T t);

	public List<T> getList()
	{
		return this.objs;
	}
}
