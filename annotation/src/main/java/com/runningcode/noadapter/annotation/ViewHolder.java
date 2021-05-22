/*
 * Copyright 2021 ccy.All Rights Reserved
 */
package com.runningcode.noadapter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ViewHolder {
    Class<?> cls() default Object.class;

    String filed() default "";

    int type() default 0;
}