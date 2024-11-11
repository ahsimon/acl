package com.demo.lasvegas.annotator;

import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.limit.AbstractLimit;
import com.netflix.concurrency.limits.limit.VegasLimit;

import com.netflix.concurrency.limits.limiter.SimpleLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
public class VegasConcurrencyLimiterRegistry {

    protected final ConcurrentMap<String, Limiter<Void>> limiterMap = new ConcurrentHashMap<>();
    protected final VegasConcurrencyLimiterProperties properties;

    @Autowired
    public VegasConcurrencyLimiterRegistry(VegasConcurrencyLimiterProperties properties) {
        this.properties = properties;
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