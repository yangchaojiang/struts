package com.xjcy.struts.annotation;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface RequestMapping
{
	String value() default "";
}
