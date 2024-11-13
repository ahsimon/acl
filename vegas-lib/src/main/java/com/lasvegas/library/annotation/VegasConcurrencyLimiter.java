package com.lasvegas.library.annotation;

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface VegasConcurrencyLimiter {
    String name() default "default";
    String fallbackMethod() default "";
}