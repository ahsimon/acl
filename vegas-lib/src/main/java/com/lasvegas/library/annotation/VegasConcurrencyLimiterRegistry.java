package com.lasvegas.library.annotation;


import com.lasvegas.library.annotation.configure.VegasConcurrencyConfig;
import com.lasvegas.library.core.VegasConcurrency;
import com.netflix.concurrency.limits.Limit;
import com.netflix.concurrency.limits.limit.VegasLimit;

import com.netflix.concurrency.limits.limiter.SimpleLimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;



import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
public class VegasConcurrencyLimiterRegistry {
    private static final Logger logger = LoggerFactory.getLogger(VegasConcurrencyLimiterRegistry.class);
    protected final ConcurrentMap<String, VegasConcurrency> limiterMap = new ConcurrentHashMap<>();

    protected final VegasConcurrencyLimiterProperties properties;

    @Autowired
    public VegasConcurrencyLimiterRegistry(VegasConcurrencyLimiterProperties properties) {
        this.properties = properties;
    }

    public Set<VegasConcurrency> getAllVegasConcurrencyLimiters(){
        return new HashSet<>(this.limiterMap.values());
    }

    public VegasConcurrency getVegasConcurrency(String name) {
        // Attempt to retrieve the limiter from the map
        return limiterMap.computeIfAbsent(name, this::createDefaultLimiter);

    }


    private VegasConcurrency createDefaultLimiter(String name) {
        logger.info("createDefaultLimiter for {}",name);
        // Retrieve properties configuration for the specified limiter name
        VegasConcurrencyConfig config = this.properties.createVegasConcurrencyConfig(name, this.properties.findVegasProperties(name));
        VegasLimit vegasLimit =  this.properties.createVegasConcurrencyLimit(VegasLimit.newBuilder(), config, this.properties.findVegasProperties(name) );

        // Build and return the SimpleLimiter with the configured Vegas limit
        SimpleLimiter<Void> limiter = SimpleLimiter.newBuilder()
                .limit(vegasLimit)
                .build();
        return new VegasConcurrency(name,limiter,config,vegasLimit);
    }

}