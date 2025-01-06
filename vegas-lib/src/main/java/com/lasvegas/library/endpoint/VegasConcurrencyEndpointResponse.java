package com.lasvegas.library.endpoint;

import io.micrometer.common.lang.Nullable;

import java.util.Map;

public class VegasConcurrencyEndpointResponse {


       @Nullable
        private Map<String, VegasConcurrencyDetails> vegasConcurrencyLimiters;

        public VegasConcurrencyEndpointResponse() {
        }

        public VegasConcurrencyEndpointResponse(Map<String, VegasConcurrencyDetails> vegasConcurrencyLimiters) {
            this.vegasConcurrencyLimiters = vegasConcurrencyLimiters;
        }

        @Nullable
        public Map<String, VegasConcurrencyDetails> getVegasConcurrencyLimiters() {
            return vegasConcurrencyLimiters;
        }

        public void VegasConcurrencyDetails(@Nullable Map<String, VegasConcurrencyDetails> vegasConcurrencyDetails) {
            this.vegasConcurrencyLimiters = vegasConcurrencyDetails;
        }

}
