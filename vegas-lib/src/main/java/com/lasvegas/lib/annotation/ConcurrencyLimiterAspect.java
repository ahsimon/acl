package com.lasvegas.lib.annotation;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.limit.AbstractLimit;
import com.netflix.concurrency.limits.limit.VegasLimit;
import com.netflix.concurrency.limits.limiter.SimpleLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Aspect
@Component
public class ConcurrencyLimiterAspect {

    private final ConcurrentMap<String, Limiter<Void>> limiters = new ConcurrentHashMap<>();

    // Access your configuration values here
    @Value("${concurrency.limits.processRequestLimiter.initialLimit:100}")
    private int processRequestLimiterInitialLimit;

    @Value("${concurrency.limits.anotherLimiter.initialLimit:50}")
    private int anotherLimiterInitialLimit;

    @Around("@annotation(concurrencyLimited)")
    public Object limitConcurrency(ProceedingJoinPoint joinPoint, ConcurrencyLimited concurrencyLimited) {
        String key = !concurrencyLimited.name().isEmpty() ? concurrencyLimited.name() : joinPoint.getSignature().toShortString();



        Limiter<Void> limiter = limiters.computeIfAbsent(key, k -> {
            AbstractLimit vegasLimit = VegasLimit.newBuilder()
                    .initialLimit(processRequestLimiterInitialLimit)
                    .build();
            return SimpleLimiter.newBuilder()
                    .limit(vegasLimit)
                    .build();
        });

        Limiter.Listener listener = limiter.acquire(null).orElse(null);
        if (listener != null) {
            try {
                Object result = joinPoint.proceed();
                listener.onSuccess();
                return result;
            } catch (Throwable throwable) {
                listener.onDropped();
                return invokeFallback(joinPoint, concurrencyLimited.fallbackMethod());
            }
        } else {
            return invokeFallback(joinPoint, concurrencyLimited.fallbackMethod());
        }
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethodName) {
        try {
            Object target = joinPoint.getTarget();
            Method fallbackMethod = target.getClass().getMethod(fallbackMethodName);

            return fallbackMethod.invoke(target);
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fallback method invocation error"));
        }
    }
}