package com.lasvegas.library.annotation;



import com.lasvegas.library.spelresolver.SpelResolver;
import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.limiter.SimpleLimiter;
import org.aspectj.lang.annotation.Around;

import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;



import org.springframework.stereotype.Component;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Optional;


@Aspect
@Component
public class VegasConcurrencyLimiterAspect {

    private final SpelResolver spelResolver;
    private final VegasConcurrencyLimiterRegistry concurrencyLimiterRegistry;



    public VegasConcurrencyLimiterAspect(VegasConcurrencyLimiterRegistry concurrencyLimiterRegistry, SpelResolver spelResolver) {
        this.concurrencyLimiterRegistry = concurrencyLimiterRegistry;
        this.spelResolver = spelResolver;

    }


    @Pointcut(value = "@within(vegasConcurrencyLimiter) || @annotation(vegasConcurrencyLimiter)", argNames = "vegasConcurrencyLimiter")
    public void matchAnnotatedClassOrMethod(VegasConcurrencyLimiter vegasConcurrencyLimiter) {
    }


    @Around(value = "matchAnnotatedClassOrMethod(vegasConcurrencyLimiter)", argNames = "proceedingJoinPoint, vegasConcurrencyLimiter")
    public Object validateConcurrency(ProceedingJoinPoint joinPoint, VegasConcurrencyLimiter vegasConcurrencyLimiter) throws Throwable {
        // Proceed without logging if the limiter is not provided
        if (vegasConcurrencyLimiter == null) {
            return joinPoint.proceed();
        }

        // Retrieve method details for logging
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String backend = spelResolver.resolve(method, joinPoint.getArgs(), vegasConcurrencyLimiter.name());


        // Obtain the limiter for the resolved backend
        SimpleLimiter<Void> limiter = concurrencyLimiterRegistry.getLimiter(backend);
        System.out.println("limiter adaptative limit: "+ limiter.getLimit());
        // Acquire a listener from the limiter
        Optional<Limiter.Listener> listener = limiter.acquire(null);
        // Proceed with the method execution if listener is present
        if (listener.isPresent()) {

            // Acquire a listener from the limiter
            try {
                Object result = joinPoint.proceed();
                listener.get().onSuccess();
                return result;
            } catch (Throwable throwable) {
                listener.get().onDropped();
                // Invoke fallback method in case of failure
                return invokeFallback(joinPoint, vegasConcurrencyLimiter.fallbackMethod(), throwable);
            }
        }
        return joinPoint.proceed();
    }





    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethodName, Throwable throwable) throws Throwable {
        // Check if the fallback method name is provided
        if (fallbackMethodName == null || fallbackMethodName.isEmpty()) {
           // logger.warn("No fallback method provided. Throwing original exception.");
            throw throwable;
        }

        try {
            // Retrieve the target object and the fallback method to invoke
            Object target = joinPoint.getTarget();
            Method fallbackMethod = target.getClass().getMethod(fallbackMethodName);

            // Invoke the fallback method and return the result
            return fallbackMethod.invoke(target);
        } catch (NoSuchMethodException e) {
            // Log specific error for method not found
            //logger.error("Fallback method not found: {} on target: {}", fallbackMethodName, joinPoint.getTarget().getClass().getName(), e);
            throw new UnsupportedOperationException("Fallback method not found: " + fallbackMethodName, e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // Log errors related to method invocation issues
            //logger.error("Error invoking fallback method: {}", fallbackMethodName, e);
            throw e.getCause() != null ? e.getCause() : e; // Throw the original cause of the exception
        }
    }

}