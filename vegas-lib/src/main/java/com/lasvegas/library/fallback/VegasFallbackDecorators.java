package com.lasvegas.library.fallback;

import com.lasvegas.library.functions.CheckedSupplier;

import java.util.List;

public class VegasFallbackDecorators {


    private final List<VegasFallbackDecorator> fallbackDecorators;
    private final VegasFallbackDecorator defaultFallbackDecorator = new VegasDefaultFallbackDecorator();

    public VegasFallbackDecorators(List<VegasFallbackDecorator> fallbackDecorators) {
        this.fallbackDecorators = fallbackDecorators;
    }

    /**
     * find a {@link VegasFallbackDecorators} by return type of the {@link VegasFallbackDecorators} and decorate
     * supplier
     *
     * @param fallbackMethod fallback method that handles supplier's exception
     * @param supplier       original function
     * @return a function which is decorated by a {@link VegasFallbackMethod}
     */
    public CheckedSupplier<Object> decorate(VegasFallbackMethod fallbackMethod,
                                            CheckedSupplier<Object> supplier) {
        return get(fallbackMethod.getReturnType())
                .decorate(fallbackMethod, supplier);
    }

    private VegasFallbackDecorator get(Class<?> returnType) {
        return fallbackDecorators.stream().filter(it -> it.supports(returnType))
                .findFirst()
                .orElse(defaultFallbackDecorator);
    }

    public List<VegasFallbackDecorator> getFallbackDecorators() {
        return fallbackDecorators;
    }
}