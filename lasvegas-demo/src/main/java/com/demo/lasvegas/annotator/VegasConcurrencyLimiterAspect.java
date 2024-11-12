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
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, VegasConcurrencyLimiter vegasConcurrencyLimiter) throws Throwable {

        if (vegasConcurrencyLimiter == null) {
            return joinPoint.proceed();
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        logger.info("methodName {} , class {}", methodName, joinPoint.getTarget().getClass());
        logger.info("vegasConcurrencyLimiter {} ", vegasConcurrencyLimiter);
        logger.info("concurrencyLimiterRegistry {} ", concurrencyLimiterRegistry);

        String backend = spelResolver.resolve(method, joinPoint.getArgs(), vegasConcurrencyLimiter.name());

        logger.info("backend {} ", backend);
        Limiter<Void> limiter = concurrencyLimiterRegistry.getLimiter(backend);
        logger.info("limiter {} ", limiter);
        Limiter.Listener listener = limiter.acquire(null).orElse(null);
        //return invokeFallback(joinPoint, vegasConcurrencyLimiter.fallbackMethod());
        if (listener != null) {
            try {

                Object result = joinPoint.proceed();
                listener.onSuccess();
                return result;
            } catch (Throwable throwable) {
                listener.onDropped();
                return invokeFallback(joinPoint, vegasConcurrencyLimiter.fallbackMethod(), throwable);
            }
        }

        return joinPoint.proceed();
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethodName, Throwable throwable) throws Throwable {

        try {
            if (fallbackMethodName == null || fallbackMethodName.isEmpty()) {
                logger.warn("No fallback method provided.");
                throw throwable;
            }

            Object target = joinPoint.getTarget();
            Method fallbackMethod = target.getClass().getMethod(fallbackMethodName);

            return fallbackMethod.invoke(target);
        } catch (Throwable e) {
            logger.error("Error invoking acl fallback method: {}", fallbackMethodName, e);
            throw e;
        }
    }

}