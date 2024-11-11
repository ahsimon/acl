package com.lasvegas.lib.annotation;


import org.springframework.beans.factory.annotation.Autowired;
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


@Aspect
@Component
public class ConcurrencyLimiterAspect {
    private final LimiterRegistry limiterRegistry;

    @Autowired
    public ConcurrencyLimiterAspect(LimiterRegistry limiterRegistry) {
        this.limiterRegistry = limiterRegistry;
    }


    public Object limitConcurrency(ProceedingJoinPoint joinPoint, ConcurrencyLimited concurrencyLimited) {
        String key = !concurrencyLimited.name().isEmpty() ? concurrencyLimited.name() : joinPoint.getSignature().toShortString();
        Limiter<Void> limiter = limiterRegistry.getLimiter(key);

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