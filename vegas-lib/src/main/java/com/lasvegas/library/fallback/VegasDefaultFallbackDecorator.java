package com.lasvegas.library.fallback;

import com.lasvegas.library.exception.IllegalReturnTypeException;
import com.lasvegas.library.functions.CheckedSupplier;

public class VegasDefaultFallbackDecorator implements VegasFallbackDecorator {


    @Override
    public boolean supports(Class<?> target) {
        return true;
    }

    @Override
    public CheckedSupplier<Object> decorate(VegasFallbackMethod fallbackMethod,
                                            CheckedSupplier<Object> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (IllegalReturnTypeException e) {
                throw e;
            } catch (Throwable throwable) {
                return fallbackMethod.fallback(throwable);
            }
        };
    }
}