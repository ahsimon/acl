package com.lasvegas.library.fallback;


import com.lasvegas.library.functions.CheckedSupplier;

public class VegasDefaultFallbackDecorator implements VegasFallbackDecorator {


    @Override
    public boolean supports(Class<?> target) {
        return true;
    }

    @Override
    public CheckedSupplier<Object> decorate(VegasFallbackMethod fallbackMethod, Throwable throwable) {
        return () -> {
                return fallbackMethod.fallback(throwable);
        };
    }
}