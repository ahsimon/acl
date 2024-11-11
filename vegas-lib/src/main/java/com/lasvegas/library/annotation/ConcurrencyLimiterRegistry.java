package com.lasvegas.library.annotation;

import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.limit.AbstractLimit;
import com.netflix.concurrency.limits.limit.VegasLimit;

import com.netflix.concurrency.limits.limiter.SimpleLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
public class ConcurrencyLimiterRegistry  {

    protected final ConcurrentMap<String, Limiter<Void>> limiterMap = new ConcurrentHashMap<>();
    protected final ConcurrencyLimiterProperties properties;

    public ConcurrencyLimiterRegistry(ConcurrencyLimiterProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ConcurrencyLimiterRegistry concurrencyLimiterRegistry(
            ConcurrencyLimiterProperties properties) {
        return new ConcurrencyLimiterRegistry(properties);
    }

    public Limiter<Void> getLimiter(String name) {
        return limiterMap.getOrDefault(name, createDefaultLimiter());
    }

    private Limiter<Void> createDefaultLimiter() {
        AbstractLimit vegasLimit =
                VegasLimit.newBuilder().initialLimit(properties.getDefaultLimit().getInitialLimit()).build();
        return   SimpleLimiter.newBuilder()
                .limit(vegasLimit)
                .build();
    }

}