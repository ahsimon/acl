package com.lasvegas.library.annotation;
import com.lasvegas.library.annotation.configure.VegasConcurrencyConfig;
import com.lasvegas.library.exception.ConfigurationNotFoundException;
import com.lasvegas.library.utils.ConfigUtils;
import io.micrometer.common.util.StringUtils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.lasvegas.library.annotation.configure.VegasConcurrencyConfig.custom;
import static com.lasvegas.library.annotation.configure.VegasConcurrencyConfig.from;
@Component
@ConfigurationProperties(prefix = "concurrency.vegas")
public class VegasConcurrencyLimiterProperties {

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