package com.lasvegas.library.annotation;



import com.lasvegas.library.fallback.VegasFallbackExecutor;
import com.lasvegas.library.spelresolver.SpelResolver;
import com.lasvegas.library.utils.AnnotationExtractor;
import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.limiter.SimpleLimiter;
import org.aspectj.lang.annotation.Around;

import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.lang.reflect.Proxy;
import java.util.Optional;


@Aspect
@Component
public class VegasConcurrencyLimiterAspect {
    private static final Logger logger = LoggerFactory.getLogger(VegasConcurrencyLimiterAspect.class);

    private final SpelResolver spelResolver;
    private final VegasConcurrencyLimiterRegistry concurrencyLimiterRegistry;
    private final VegasFallbackExecutor vegasFallbackExecutor;


    public VegasConcurrencyLimiterAspect(VegasConcurrencyLimiterRegistry concurrencyLimiterRegistry, SpelResolver spelResolver, VegasFallbackExecutor vegasFallbackExecutor) {
        this.concurrencyLimiterRegistry = concurrencyLimiterRegistry;
        this.spelResolver = spelResolver;
        this.vegasFallbackExecutor =vegasFallbackExecutor;

    }


    @Pointcut(value = "@within(vegasConcurrencyLimiter) || @annotation(vegasConcurrencyLimiter)", argNames = "vegasConcurrencyLimiter")
    public void matchAnnotatedClassOrMethod(VegasConcurrencyLimiter vegasConcurrencyLimiter) {
    }


    @Around(value = "matchAnnotatedClassOrMethod(vegasConcurrencyLimiter)", argNames = "proceedingJoinPoint, vegasConcurrencyLimiter")
    public Object validateConcurrency(ProceedingJoinPoint joinPoint, VegasConcurrencyLimiter vegasConcurrencyLimiter) throws Throwable {
        // Proceed without logging if the limiter is not provided

        if (vegasConcurrencyLimiter == null) {
            vegasConcurrencyLimiter = getVegasConcurrencyLimiterAnnotation(joinPoint);
        }
        if (vegasConcurrencyLimiter == null) {
            return joinPoint.proceed();
        }

        // Retrieve method details for logging
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String backend = spelResolver.resolve(method, joinPoint.getArgs(), vegasConcurrencyLimiter.name());

        // Obtain the limiter for the resolved backend
        SimpleLimiter<Void> limiter = concurrencyLimiterRegistry.getLimiter(backend);
        logger.info("Vegas adaptative limiter: current limit:{} ", limiter.getLimit());
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
                return invokeFallback(joinPoint, method, vegasConcurrencyLimiter.fallbackMethod(), throwable);
            }
        }
        return joinPoint.proceed();
    }




    private VegasConcurrencyLimiter getVegasConcurrencyLimiterAnnotation(ProceedingJoinPoint proceedingJoinPoint) {
        if (logger.isDebugEnabled()) {
            logger.debug("VegasConcurrencyLimiterAnnotation parameter is null");
        }
        if (proceedingJoinPoint.getTarget() instanceof Proxy) {
            logger.debug(
                    "The circuit breaker annotation is kept on a interface which is acting as a proxy");
            return AnnotationExtractor
                    .extractAnnotationFromProxy(proceedingJoinPoint.getTarget(), VegasConcurrencyLimiter.class);
        } else {
            return AnnotationExtractor
                    .extract(proceedingJoinPoint.getTarget().getClass(), VegasConcurrencyLimiter.class);
        }
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint,  Method method , String fallbackMethodValue, Throwable throwable) throws Throwable {
        // Check if the fallback method name is provided
        if (fallbackMethodValue == null || fallbackMethodValue.isEmpty()) {
           // logger.warn("No fallback method provided. Throwing original exception.");
            throw throwable;
        }


        try {

            // Invoke the fallback method and return the result
            //return fallbackMethod.invoke(target);
            return vegasFallbackExecutor.execute(joinPoint,method, fallbackMethodValue,throwable);
        } catch (NoSuchMethodException e) {
            // Log specific error for method not found
            //logger.error("Fallback method not found: {} on target: {}", fallbackMethodName, joinPoint.getTarget().getClass().getName(), e);
            throw new UnsupportedOperationException("Fallback method not found: " + fallbackMethodValue, e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // Log errors related to method invocation issues
            //logger.error("Error invoking fallback method: {}", fallbackMethodName, e);
            throw e.getCause() != null ? e.getCause() : e; // Throw the original cause of the exception
        }
    }

}