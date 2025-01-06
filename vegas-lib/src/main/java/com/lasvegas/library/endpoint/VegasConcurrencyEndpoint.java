package com.lasvegas.library.endpoint;
import com.lasvegas.library.annotation.VegasConcurrencyLimiterRegistry;
import com.lasvegas.library.annotation.configure.VegasConcurrencyConfig;
import com.lasvegas.library.core.VegasConcurrency;

import com.netflix.concurrency.limits.limiter.SimpleLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;


import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Endpoint(id = "vegaslimiters")
public class VegasConcurrencyEndpoint {



    private final VegasConcurrencyLimiterRegistry vegasConcurrencyLimiterRegistry;


    public VegasConcurrencyEndpoint(VegasConcurrencyLimiterRegistry vegasConcurrencyLimiterRegistry) {
        this.vegasConcurrencyLimiterRegistry = vegasConcurrencyLimiterRegistry;
    }

    @ReadOperation
    public VegasConcurrencyEndpointResponse getAllVegasConcurrencyLimiters() {
        Map<String, VegasConcurrencyDetails> vegasLimiters = vegasConcurrencyLimiterRegistry.getAllVegasConcurrencyLimiters().stream()
                .sorted(Comparator.comparing(VegasConcurrency::getName))
                .collect(Collectors.toMap(VegasConcurrency::getName, this::createVegasConcurrencyDetails, (v1,v2) -> v1, LinkedHashMap::new));
        return new VegasConcurrencyEndpointResponse(vegasLimiters);
    }



    private VegasConcurrencyDetails createVegasConcurrencyDetails(VegasConcurrency vegasConcurrency) {

        VegasConcurrencyConfig config = vegasConcurrency.getConfig();
        SimpleLimiter<Void> limiter = vegasConcurrency.getLimiter();
        VegasConcurrencyDetails vegasConcurrencyDetails = new VegasConcurrencyDetails();
        vegasConcurrencyDetails.setCurrentLimit(limiter.getLimit());
        vegasConcurrencyDetails.setMaxLimit(config.getMaxConcurrency());
        vegasConcurrencyDetails.setInitialLimit(config.getInitialLimit());
        vegasConcurrencyDetails.setSmoothing(config.getSmoothing());
        return vegasConcurrencyDetails;
    }

}
