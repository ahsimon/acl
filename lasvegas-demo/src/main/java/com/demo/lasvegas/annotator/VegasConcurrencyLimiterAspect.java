package com.demo.lasvegas.annotator;


import com.demo.lasvegas.annotator.spelresolver.SpelResolver;
import com.netflix.concurrency.limits.Limiter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Aspect
@Component
public class VegasConcurrencyLimiterAspect {
    private static final Logger logger = LoggerFactory.getLogger(VegasConcurrencyLimiterAspect.class);
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
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        logger.info("Executing method: {}, class: {}", methodName, joinPoint.getTarget().getClass());

        // Resolve backend using SpEL
        String backend = spelResolver.resolve(method, joinPoint.getArgs(), vegasConcurrencyLimiter.name());


        // Obtain the limiter for the resolved backend
        Limiter<Void> limiter = concurrencyLimiterRegistry.getLimiter(backend);
        logger.info("Limiter for backend '{}': {}", backend, limiter);

        // Acquire a listener from the limiter
        Limiter.Listener listener = limiter.acquire(null).orElse(null);

        // Proceed with the method execution if listener is present
        if (listener != null) {
            try {
                Object result = joinPoint.proceed();
                listener.onSuccess();
                return result;
            } catch (Throwable throwable) {
                listener.onDropped();
                // Invoke fallback method in case of failure
                return invokeFallback(joinPoint, vegasConcurrencyLimiter.fallbackMethod(), throwable);
            }
        }

        // Proceed without a listener if not acquired
        return joinPoint.proceed();
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethodName, Throwable throwable) throws Throwable {
        // Check if the fallback method name is provided
        if (fallbackMethodName == null || fallbackMethodName.isEmpty()) {
            logger.warn("No fallback method provided. Throwing original exception.");
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
            logger.error("Fallback method not found: {} on target: {}", fallbackMethodName, joinPoint.getTarget().getClass().getName(), e);
            throw new UnsupportedOperationException("Fallback method not found: " + fallbackMethodName, e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // Log errors related to method invocation issues
            logger.error("Error invoking fallback method: {}", fallbackMethodName, e);
            throw e.getCause() != null ? e.getCause() : e; // Throw the original cause of the exception
        }
    }

}