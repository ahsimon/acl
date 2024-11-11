package com.lasvegas.library.annotation;

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface ConcurrencyLimiter {
    int initialLimit() default 100;
    String name() default "";
    String fallbackMethod() default "";
}