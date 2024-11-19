package com.lasvegas.library.fallback;

import com.lasvegas.library.functions.CheckedSupplier;

public interface VegasFallbackDecorator  {

    boolean supports(Class<?> target);

    /**
     * @param fallbackMethod fallback method.
     * @param supplier       target function should be decorated.
     * @return decorated function
     */
    CheckedSupplier<Object> decorate(VegasFallbackMethod fallbackMethod,
                                     CheckedSupplier<Object> supplier);
}