package com.demo.lasvegas.annotator.fallback;

import com.demo.lasvegas.annotator.functions.CheckedSupplier;

/**
 * interface of FallbackDecorator
 */
public interface FallbackDecorator {

    boolean supports(Class<?> target);

    /**
     * @param fallbackMethod fallback method.
     * @param supplier       target function should be decorated.
     * @return decorated function
     */
    CheckedSupplier<Object> decorate(FallbackMethod fallbackMethod,
                                     CheckedSupplier<Object> supplier);
}