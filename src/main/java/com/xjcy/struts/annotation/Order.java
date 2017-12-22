package com.xjcy.struts.annotation;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface Order
{
	int value() default 0;
}

