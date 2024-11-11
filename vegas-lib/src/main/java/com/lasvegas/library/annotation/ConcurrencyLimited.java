package com.lasvegas.library.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConcurrencyLimited {
    int initialLimit() default 100;
    String name() default "";
    String fallbackMethod() default "";
}