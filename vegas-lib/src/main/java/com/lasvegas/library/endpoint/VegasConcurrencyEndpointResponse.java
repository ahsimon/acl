package com.lasvegas.library.endpoint;

import io.micrometer.common.lang.Nullable;

import java.util.Map;

public class VegasConcurrencyEndpointResponse {


       @Nullable
        private Map<String, VegasConcurrencyDetails> vegasConcurrencyDetails;

        public VegasConcurrencyEndpointResponse() {
        }

        public VegasConcurrencyEndpointResponse(Map<String, VegasConcurrencyDetails> vegasConcurrencyDetails) {
            this.vegasConcurrencyDetails = vegasConcurrencyDetails;
        }

        @Nullable
        public Map<String, VegasConcurrencyDetails> getVegasConcurrencyDetails() {
            return vegasConcurrencyDetails;
        }

        public void VegasConcurrencyDetails(@Nullable Map<String, VegasConcurrencyDetails> vegasConcurrencyDetails) {
            this.vegasConcurrencyDetails = vegasConcurrencyDetails;
        }

}
