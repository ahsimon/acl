package com.lasvegas.library.fallback;

import com.lasvegas.library.annotation.VegasConcurrencyLimiterAspect;
import com.lasvegas.library.functions.CheckedSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VegasFallbackDecorators {
    private static final Logger logger = LoggerFactory.getLogger(VegasConcurrencyLimiterAspect.class);

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

     * @return a function which is decorated by a {@link VegasFallbackMethod}
     */
    public CheckedSupplier<Object> decorate(VegasFallbackMethod fallbackMethod, Throwable throwable) {
        VegasFallbackDecorator decorator = get(fallbackMethod.getReturnType());
        logger.debug("decorator = {}" , decorator);
              return   decorator.decorate(fallbackMethod,throwable);
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