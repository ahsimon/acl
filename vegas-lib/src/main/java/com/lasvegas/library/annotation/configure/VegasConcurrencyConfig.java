package com.lasvegas.library.annotation.configure;


import java.io.Serializable;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;


import com.netflix.concurrency.limits.MetricRegistry;
import com.netflix.concurrency.limits.internal.EmptyMetricRegistry;
import com.netflix.concurrency.limits.limit.functions.Log10RootIntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VegasConcurrencyConfig implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(VegasConcurrencyConfig.class);

    private static final IntUnaryOperator LOG10 = Log10RootIntFunction.create(0);


    private VegasConcurrencyConfig() {
    }
     int initialLimit = 20;
     int maxConcurrency = 1000;
     double smoothing = 1.0;
     int alpha =3;
     int beta =6;


     IntUnaryOperator alphaFunc = (limit) -> 3 * LOG10.applyAsInt(limit);
     IntUnaryOperator betaFunc = (limit) -> 6 * LOG10.applyAsInt(limit);
     IntUnaryOperator thresholdFunc = LOG10;
     DoubleUnaryOperator increaseFunc = (limit) -> limit + LOG10.applyAsInt((int) limit);
     DoubleUnaryOperator decreaseFunc = (limit) -> limit - LOG10.applyAsInt((int) limit);
     int probeMultiplier = 30;

     MetricRegistry registry = EmptyMetricRegistry.INSTANCE;

    public int getInitialLimit() {
        return initialLimit;
    }

    public void setInitialLimit(int initialLimit) {
        this.initialLimit = initialLimit;
    }

    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    public void setMaxConcurrency(int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
    }

    public double getSmoothing() {
        return smoothing;
    }

    public void setSmoothing(double smoothing) {
        this.smoothing = smoothing;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getBeta() {
        return beta;
    }

    public void setBeta(int beta) {
        this.beta = beta;
    }

    public IntUnaryOperator getAlphaFunc() {
        return alphaFunc;
    }

    public void setAlphaFunc(IntUnaryOperator alphaFunc) {
        this.alphaFunc = alphaFunc;
    }

    public IntUnaryOperator getBetaFunc() {
        return betaFunc;
    }

    public void setBetaFunc(IntUnaryOperator betaFunc) {
        this.betaFunc = betaFunc;
    }

    public IntUnaryOperator getThresholdFunc() {
        return thresholdFunc;
    }

    public void setThresholdFunc(IntUnaryOperator thresholdFunc) {
        this.thresholdFunc = thresholdFunc;
    }

    public DoubleUnaryOperator getIncreaseFunc() {
        return increaseFunc;
    }

    public void setIncreaseFunc(DoubleUnaryOperator increaseFunc) {
        this.increaseFunc = increaseFunc;
    }

    public DoubleUnaryOperator getDecreaseFunc() {
        return decreaseFunc;
    }

    public void setDecreaseFunc(DoubleUnaryOperator decreaseFunc) {
        this.decreaseFunc = decreaseFunc;
    }

    public int getProbeMultiplier() {
        return probeMultiplier;
    }

    public void setProbeMultiplier(int probeMultiplier) {
        this.probeMultiplier = probeMultiplier;
    }

    public MetricRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(MetricRegistry registry) {
        this.registry = registry;
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder from(VegasConcurrencyConfig baseConfig) {
        return new Builder(baseConfig);
    }
    public static class Builder {


         int initialLimit = 20;
         int maxConcurrency = 1000;
         double smoothing = 1.0;
         int alpha =3;
         int beta =6;

         IntUnaryOperator alphaFunc = (limit) -> 3 * LOG10.applyAsInt(limit);
         IntUnaryOperator betaFunc = (limit) -> 6 * LOG10.applyAsInt(limit);
         IntUnaryOperator thresholdFunc = LOG10;
         DoubleUnaryOperator increaseFunc = (limit) -> limit + LOG10.applyAsInt((int) limit);
         DoubleUnaryOperator decreaseFunc = (limit) -> limit - LOG10.applyAsInt((int) limit);
         int probeMultiplier = 30;
         MetricRegistry registry = EmptyMetricRegistry.INSTANCE;
        public VegasConcurrencyConfig build() {
            VegasConcurrencyConfig config = new VegasConcurrencyConfig();

            config.initialLimit = initialLimit;
            config.probeMultiplier = probeMultiplier;
            config.maxConcurrency = maxConcurrency;
            config.smoothing = smoothing;
            config.alpha = alpha;
            config.beta = beta;
            config.alphaFunc = alphaFunc;
            config.betaFunc = betaFunc;
            config.thresholdFunc = thresholdFunc;
            config.increaseFunc = increaseFunc;
            config.decreaseFunc = decreaseFunc;
            config.registry = registry;

            return config;
        }

        public  Builder () {

        }

        public static Builder from(VegasConcurrencyConfig baseConfig) {
            return new Builder(baseConfig);
        }


        public Builder(VegasConcurrencyConfig baseConfig) {
            this.initialLimit =baseConfig.getInitialLimit();
            this.probeMultiplier = baseConfig.getProbeMultiplier();
            this.maxConcurrency = baseConfig.getMaxConcurrency();
            this.smoothing = baseConfig.getSmoothing();
            this.alpha = baseConfig.getAlpha();
            this.beta = baseConfig.getBeta();
            this.alphaFunc = baseConfig.getAlphaFunc();
            this.betaFunc = baseConfig.getBetaFunc();
            this.thresholdFunc = baseConfig.getThresholdFunc();
            this.increaseFunc = baseConfig.getIncreaseFunc();
            this.decreaseFunc = baseConfig.getDecreaseFunc();
            this.registry = baseConfig.registry;

            if (this.initialLimit > this.maxConcurrency) {
                logger.warn("Initial limit {} exceeded maximum limit {}", initialLimit, maxConcurrency);
            }
        }


        public Builder probeMultiplier(int probeMultiplier) {
            this.probeMultiplier = probeMultiplier;
            return this;
        }

        public Builder alpha(int alpha) {
            this.alphaFunc = (ignore) -> alpha;
            return this;
        }

        public Builder thresholdFunction(IntUnaryOperator threshold) {
            this.thresholdFunc = threshold;
            return this;
        }

        public Builder alphaFunction(IntUnaryOperator alpha) {
            this.alphaFunc = alpha;
            return this;
        }

        public Builder beta(int beta) {
            this.betaFunc = (ignore) -> beta;
            return this;
        }

        public Builder betaFunction(IntUnaryOperator beta) {
            this.betaFunc = beta;
            return this;
        }

        public Builder increaseFunction(DoubleUnaryOperator increase) {
            this.increaseFunc = increase;
            return this;
        }

        public Builder decreaseFunction(DoubleUnaryOperator decrease) {
            this.decreaseFunc = decrease;
            return this;
        }

        public Builder smoothing(double smoothing) {
            this.smoothing = smoothing;
            return this;
        }

        public Builder initialLimit(int initialLimit) {
            this.initialLimit = initialLimit;
            return this;
        }
        public Builder maxConcurrency(int maxConcurrency) {
            this.maxConcurrency = maxConcurrency;
            return this;
        }
        public Builder metricRegistry(MetricRegistry registry) {
            this.registry = registry;
            return this;
        }

    }


    @Override
    public String toString() {
        return "VegasConcurrencyConfig{" +
                "initialLimit=" + initialLimit +
                ", maxConcurrency=" + maxConcurrency +
                ", smoothing=" + smoothing +
                ", alpha=" + alpha +
                ", beta=" + beta +
                ", alphaFunc=" + alphaFunc +
                ", betaFunc=" + betaFunc +
                ", thresholdFunc=" + thresholdFunc +
                ", increaseFunc=" + increaseFunc +
                ", decreaseFunc=" + decreaseFunc +
                ", probeMultiplier=" + probeMultiplier +
                ", registry=" + registry +
                '}';
    }
}