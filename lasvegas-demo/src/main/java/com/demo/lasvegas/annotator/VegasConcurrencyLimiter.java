package com.demo.lasvegas.annotator;

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface VegasConcurrencyLimiter {
    int initialLimit() default 100;
    String name() default "default";
    String fallbackMethod() default "";
}