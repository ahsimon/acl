package com.lasvegas.library.fallback;

import com.lasvegas.library.functions.CheckedSupplier;

public interface VegasFallbackDecorator {

    boolean supports(Class<?> target);

    /**
     * @param fallbackMethod fallback method.
     * @return decorated function
     */
    CheckedSupplier<Object> decorate(VegasFallbackMethod fallbackMethod,
                                  Throwable throwable);
}