package com.lasvegas.lib.annotation;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;
@Component
@ConfigurationProperties(prefix = "concurrency.limits")
public class ConcurrencyLimitProperties {

    private DefaultLimit defaultLimit = new DefaultLimit();
    private Map<String, Integer> limiters;

    public DefaultLimit getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(DefaultLimit defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public Map<String, Integer> getLimiters() {
        return limiters;
    }

    public void setLimiters(Map<String, Integer> limiters) {
        this.limiters = limiters;
    }

    public static class DefaultLimit {
        private int initialLimit = 100;

        public int getInitialLimit() {
            return initialLimit;
        }

        public void setInitialLimit(int initialLimit) {
            this.initialLimit = initialLimit;
        }
    }
}