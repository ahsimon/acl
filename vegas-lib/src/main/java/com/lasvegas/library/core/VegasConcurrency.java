package com.lasvegas.library.core;

import com.lasvegas.library.annotation.configure.VegasConcurrencyConfig;
import com.netflix.concurrency.limits.limiter.SimpleLimiter;

public class VegasConcurrency {

    SimpleLimiter<Void> limiter;
    VegasConcurrencyConfig config;
    String name;

    public VegasConcurrency(String name, SimpleLimiter<Void> limiter, VegasConcurrencyConfig config) {
        this.limiter = limiter;
        this.config = config;
        this.name = name;
    }

    public SimpleLimiter<Void> getLimiter() {
        return limiter;
    }


    public VegasConcurrencyConfig getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }
}
