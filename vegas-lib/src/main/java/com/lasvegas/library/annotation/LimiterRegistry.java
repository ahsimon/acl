package com.lasvegas.library.annotation;
import com.netflix.concurrency.limits.Limit;
import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.limit.AbstractLimit;
import com.netflix.concurrency.limits.limit.VegasLimit;
import com.netflix.concurrency.limits.limiter.SimpleLimiter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LimiterRegistry {

    private final ConcurrentMap<String, Limiter<Void>> limiters = new ConcurrentHashMap<>();

    private final ConcurrencyLimitProperties properties;

    @Autowired
    public LimiterRegistry(ConcurrencyLimitProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() {
        for (Map.Entry<String, Integer> entry : properties.getLimiters().entrySet()) {
            registerLimiter(entry.getKey(), entry.getValue());
        }
    }

    public void registerLimiter(String name, int initialLimit) {
        AbstractLimit vegasLimit = VegasLimit.newBuilder()
                .initialLimit(initialLimit)
                .build();
        limiters.put(name,  SimpleLimiter.newBuilder()
                .limit(vegasLimit)
                .build());
    }

    public Limiter<Void> getLimiter(String name) {
        return limiters.getOrDefault(name, createDefaultLimiter());
    }

    private Limiter<Void> createDefaultLimiter() {
        Limit vegasLimit= VegasLimit.newBuilder()
                .initialLimit(properties.getDefaultLimit().getInitialLimit())
                .build();

      return   SimpleLimiter.newBuilder()
                .limit(vegasLimit)
                .build();
    }
}