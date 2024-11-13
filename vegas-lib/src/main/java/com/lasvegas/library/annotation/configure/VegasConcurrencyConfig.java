package com.lasvegas.library.annotation.configure;


import java.io.Serializable;

public class VegasConcurrencyConfig implements Serializable {



    private VegasConcurrencyConfig() {
    }
    private int initialLimit = 10;
    private int maxConcurrency = 1000;
    private double smoothing = 1.0;
    private int alpha =3;
    private int beta =6;


    public int getBeta() {
        return beta;
    }

    public void setBeta(int beta) {
        this.beta = beta;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }



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

    public static Builder custom() {
        return new Builder();
    }

    public static Builder from(VegasConcurrencyConfig baseConfig) {
        return new Builder(baseConfig);
    }
    public static class Builder {


        private int initialLimit = 10;
        private int maxConcurrency = 1000;
        private double smoothing = 1.0;
        private int alpha = 3;
        private int beta=6;


        public VegasConcurrencyConfig build() {
            VegasConcurrencyConfig config = new VegasConcurrencyConfig();

            config.initialLimit = initialLimit;
            config.smoothing = smoothing;
            config.maxConcurrency=maxConcurrency;
            config.alpha = alpha;
            config.beta =beta;
            return config;
        }

        public  Builder () {

        }

        public static Builder from(VegasConcurrencyConfig baseConfig) {
            return new Builder(baseConfig);
        }


        public Builder(VegasConcurrencyConfig baseConfig) {
            this.initialLimit =baseConfig.getInitialLimit();
            this.maxConcurrency = baseConfig.getMaxConcurrency();
            this.smoothing = baseConfig.getSmoothing();
            this.alpha = baseConfig.getAlpha();
            this.beta = baseConfig.getBeta();
        }

        public Builder initialLimit(int initialLimit) {
            if (initialLimit < 1) {
                throw new IllegalArgumentException("initialLimit must be greater than 0");
            }
            this.initialLimit = initialLimit;
            return this;
        }

        public Builder maxConcurrency(int maxConcurrency) {
            if (maxConcurrency > 0) {
                this.maxConcurrency = maxConcurrency;
            }
            return this;
        }

        public Builder smoothing(double smoothing) {
            if (smoothing >= 1.0){
                this.smoothing = smoothing;
            }
            return this;
        }

        public Builder alpha(int alpha) {
            if (alpha >= 0){
                this.alpha = alpha;
            }
            return this;
        }

        public Builder beta(int beta) {
            if (beta >= 0){
                this.beta = beta;
            }
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
                '}';
    }
}