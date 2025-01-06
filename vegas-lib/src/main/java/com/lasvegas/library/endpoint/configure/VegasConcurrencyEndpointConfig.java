package com.lasvegas.library.endpoint.configure;


import com.lasvegas.library.annotation.VegasConcurrencyLimiterRegistry;
import com.lasvegas.library.core.VegasConcurrency;
import com.lasvegas.library.endpoint.VegasConcurrencyEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
@ConditionalOnClass(VegasConcurrency.class)

public class VegasConcurrencyEndpointConfig {

    @Configuration
    @ConditionalOnClass(Endpoint.class)
    static class  VegasConcurrencyEndpointAutoConfiguration {

        @Bean
        @ConditionalOnAvailableEndpoint
        public VegasConcurrencyEndpoint vegasConcurrencyEndpoint(
                VegasConcurrencyLimiterRegistry vegasConcurrencyLimiterRegistry) {
            return new VegasConcurrencyEndpoint(vegasConcurrencyLimiterRegistry);
        }
    }
}