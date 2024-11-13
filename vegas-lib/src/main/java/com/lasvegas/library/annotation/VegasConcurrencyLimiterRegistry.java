package com.lasvegas.library.annotation;


import com.lasvegas.library.annotation.configure.VegasConcurrencyConfig;
import com.netflix.concurrency.limits.limit.VegasLimit;

import com.netflix.concurrency.limits.limiter.SimpleLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
public class VegasConcurrencyLimiterRegistry {
    private static final Logger logger = LoggerFactory.getLogger(VegasConcurrencyLimiterRegistry.class);
    protected final ConcurrentMap<String, SimpleLimiter<Void>> limiterMap = new ConcurrentHashMap<>();
    protected final VegasConcurrencyLimiterProperties properties;

    @Autowired
    public VegasConcurrencyLimiterRegistry(VegasConcurrencyLimiterProperties properties) {
        this.properties = properties;
    }

    public SimpleLimiter<Void> getLimiter(String name) {
        // Attempt to retrieve the limiter from the map
        return limiterMap.computeIfAbsent(name, this::createDefaultLimiter);
    }


    private SimpleLimiter<Void> createDefaultLimiter(String name) {

        // Retrieve properties configuration for the specified limiter name
        VegasConcurrencyConfig config = this.properties.createVegasConcurrencyConfig(name, this.properties.findVegasProperties(name));


        VegasLimit vegasLimit = VegasLimit.newBuilder()
                .initialLimit(config.getInitialLimit())
                .maxConcurrency(config.getMaxConcurrency())
                .build();

        // Build and return the SimpleLimiter with the configured Vegas limit
        return SimpleLimiter.newBuilder()
                .limit(vegasLimit)
                .build();
    }

}