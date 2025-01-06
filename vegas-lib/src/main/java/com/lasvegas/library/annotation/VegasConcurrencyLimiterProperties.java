package com.lasvegas.library.annotation;
import com.lasvegas.library.annotation.configure.VegasConcurrencyConfig;
import com.lasvegas.library.exception.ConfigurationNotFoundException;
import com.lasvegas.library.utils.ConfigUtils;
import com.netflix.concurrency.limits.MetricRegistry;
import com.netflix.concurrency.limits.internal.EmptyMetricRegistry;
import com.netflix.concurrency.limits.limit.VegasLimit;
import io.micrometer.common.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;

import static com.lasvegas.library.annotation.configure.VegasConcurrencyConfig.custom;
import static com.lasvegas.library.annotation.configure.VegasConcurrencyConfig.from;
@Component
@ConfigurationProperties(prefix = "concurrency.vegas")
public class VegasConcurrencyLimiterProperties {
    static final Logger logger = LoggerFactory.getLogger(VegasConcurrencyLimiterProperties.class);

    private static final String DEFAULT = "default";
    private Map<String, InstanceProperties> instances = new HashMap<>();
    private Map<String, InstanceProperties> configs = new HashMap<>();



    public static class InstanceProperties {

        private Integer initialLimit ;
        private Integer maxConcurrency ;
        private Double smoothing ;
        private String baseConfig;
        private Integer alpha;
        private Integer beta;
        Integer probeMultiplier;


        public Integer getProbeMultiplier() {
            return probeMultiplier;
        }

        public void setProbeMultiplier(Integer probeMultiplier) {
            this.probeMultiplier = probeMultiplier;
        }
        
        
        public Integer getInitialLimit() {
            return initialLimit;
        }

        public void setInitialLimit(Integer initialLimit) {
            this.initialLimit = initialLimit;
        }

        public Integer getMaxConcurrency() {
            return maxConcurrency;
        }

        public void setMaxConcurrency(Integer maxConcurrency) {
            this.maxConcurrency = maxConcurrency;
        }

        public Double getSmoothing() {
            return smoothing;
        }

        public void setSmoothing(Double smoothing) {
            this.smoothing = smoothing;
        }

        public void setBaseConfig(String baseConfig) {
            this.baseConfig = baseConfig;
        }

        public Integer getAlpha() {
            return alpha;
        }

        public void setAlpha(Integer alpha) {
            this.alpha = alpha;
        }

        public Integer getBeta() {
            return beta;
        }

        public void setBeta(Integer beta) {
            this.beta = beta;
        }

        public String getBaseConfig() {
            return baseConfig;
        }

        @Override
        public String toString() {
            return "InstanceProperties{" +
                    "initialLimit=" + initialLimit +
                    ", maxConcurrency=" + maxConcurrency +
                    ", smoothing=" + smoothing +
                    ", baseConfig='" + baseConfig + '\'' +
                    ", alpha=" + alpha +
                    ", beta=" + beta +
                    ", probeMultiplier=" + probeMultiplier +
                    '}';
        }
    }

    

    public void setInstances(Map<String, InstanceProperties> instances) {
        this.instances = instances;
    }

    public void setConfigs(Map<String, InstanceProperties> configs) {
        this.configs = configs;
    }



    public InstanceProperties findVegasProperties(String name) {
        InstanceProperties instanceProperties = instances.get(name);
        if (instanceProperties == null) {
            instanceProperties = configs.get(DEFAULT);
        }
        return instanceProperties;
    }





    public VegasConcurrencyConfig createVegasConcurrencyConfig(String instanceName,
                                                               InstanceProperties instanceProperties) {

        VegasConcurrencyConfig baseConfig = null;
        if (instanceProperties != null && StringUtils.isNotEmpty(instanceProperties.getBaseConfig())) {
            baseConfig = createBaseConfig(instanceName, instanceProperties);
        } else if (configs.get(instanceName) != null) {
            baseConfig = createDirectConfig(instanceName, instanceProperties);
        } else if (configs.get(DEFAULT) != null) {
            baseConfig = createDefaultConfig(instanceProperties);
        }

        return buildConfig(baseConfig != null ? from(baseConfig) : custom(), instanceProperties);
    }


    public VegasLimit createVegasConcurrencyLimit(VegasLimit.Builder builder, VegasConcurrencyConfig config, InstanceProperties vegasProperties) {

        if(config != null){
            // add  builder basic configs
            builder.initialLimit(config.getInitialLimit());
            builder.maxConcurrency(config.getMaxConcurrency());
            builder.alphaFunction(config.getAlphaFunc());
            builder.betaFunction(config.getBetaFunc());
            builder.increaseFunction(config.getIncreaseFunc());
            builder.decreaseFunction(config.getDecreaseFunc());
            builder.thresholdFunction(config.getThresholdFunc());
            builder.smoothing(config.getSmoothing());
            builder.probeMultiplier(config.getProbeMultiplier());
            // only explicit update for alpha and beta
            if (vegasProperties != null ) {
                if (vegasProperties.getAlpha() != null) {
                    builder.alpha(vegasProperties.getAlpha());
                }
                if (vegasProperties.getBeta() != null) {
                    builder.beta(vegasProperties.getBeta());
                }
            }
        }
        return builder.build();
    }

    public VegasLimit createBasicVegasLimit(  VegasLimit.Builder builder,
                                         InstanceProperties properties ){


        if (properties != null) {


            if (properties.getInitialLimit() != null) {
                builder.initialLimit(properties.getInitialLimit());
            }

            if (properties.getMaxConcurrency() != null) {
                builder.maxConcurrency(properties.getMaxConcurrency());
            }

            if (properties.getSmoothing() != null) {
                builder.smoothing(properties.getSmoothing());
            }

            if (properties.getAlpha() != null) {
                builder.beta(properties.getAlpha());
            }

            if (properties.getBeta() != null) {
                builder.beta(properties.getBeta());
            }

            if(properties.getProbeMultiplier() != null){
                builder.probeMultiplier(properties.getProbeMultiplier());
            }
        }

        return builder.build();


    }


    private VegasConcurrencyConfig createBaseConfig(String instanceName,
                                                    InstanceProperties instanceProperties) {

        String baseConfigName = instanceProperties.getBaseConfig();
        if (instanceName.equals(baseConfigName)) {
            throw new IllegalStateException("Circular reference detected in instance config: " + instanceName);
        }

        InstanceProperties baseProperties = configs.get(baseConfigName);
        if (baseProperties == null) {
            throw new ConfigurationNotFoundException(baseConfigName);
        }

        ConfigUtils.mergePropertiesIfAny(instanceProperties, baseProperties);
        return createVegasConcurrencyConfig(baseConfigName, baseProperties);
    }


    private VegasConcurrencyConfig createDirectConfig(String instanceName,  InstanceProperties instanceProperties ) {

        if (instanceProperties != null) {
            ConfigUtils.mergePropertiesIfAny(instanceProperties, configs.get(instanceName));
        }
        return buildConfig(custom(), configs.get(instanceName));
    }

    private VegasConcurrencyConfig createDefaultConfig( InstanceProperties instanceProperties) {

        if (instanceProperties != null) {
            ConfigUtils.mergePropertiesIfAny(instanceProperties, configs.get(DEFAULT));
        }
        return createVegasConcurrencyConfig(DEFAULT, configs.get(DEFAULT));
    }

    private VegasConcurrencyConfig buildConfig(VegasConcurrencyConfig.Builder builder, InstanceProperties properties) {
        if (properties != null) {


            if (properties.getInitialLimit() != null) {
                builder.initialLimit(properties.getInitialLimit());
            }

            if (properties.getMaxConcurrency() != null) {
                builder.maxConcurrency(properties.getMaxConcurrency());
            }

            if (properties.getSmoothing() != null) {
                builder.smoothing(properties.getSmoothing());
            }

            if (properties.getAlpha() != null) {
                builder.beta(properties.getAlpha());
            }

            if (properties.getBeta() != null) {
                builder.beta(properties.getBeta());
            }

            if(properties.getProbeMultiplier() != null){
                builder.probeMultiplier(properties.getProbeMultiplier());
            }
        }



        return builder.build();
    }











    public InstanceProperties getBackendProperties(String backend) {
        return instances.get(backend);
    }

    public Map<String, InstanceProperties> getInstances() {
        return instances;
    }

    /**
     * For backwards compatibility when setting backends in configuration properties.
     */
    public Map<String, InstanceProperties> getBackends() {
        return instances;
    }

    public Map<String, InstanceProperties> getConfigs() {
        return configs;
    }





}