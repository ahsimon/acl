package com.lasvegas.library.endpoint;


import java.io.Serializable;

/*
    ///In-flight Requests (IFR): Number of requests that are currently being processed
   If IFR < CL, request is allowed and IFR is incremented by 1
    If IFR == CL, request is rejected
 */
public class VegasConcurrencyDetails implements Serializable {


    //Concurrency Limit (CL): Current concurrency limit value
    int currentLimit;


    int initialLimit;
    int maxLimit;
    String limit;

    public int getCurrentLimit() {
        return currentLimit;
    }

    public void setCurrentLimit(int currentLimit) {
        this.currentLimit = currentLimit;
    }


    public int getInitialLimit() {
        return initialLimit;
    }

    public void setInitialLimit(int initialLimit) {
        this.initialLimit = initialLimit;
    }

    public int getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(int maxLimit) {
        this.maxLimit = maxLimit;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }
}
