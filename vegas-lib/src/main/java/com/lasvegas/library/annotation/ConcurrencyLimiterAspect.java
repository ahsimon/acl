package com.lasvegas.library.annotation;


import com.lasvegas.library.annotation.utils.AnnotationExtractor;
import com.netflix.concurrency.limits.Limiter;
import org.aspectj.lang.annotation.Around;

import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


@Aspect
public class ConcurrencyLimiterAspect  {


    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyLimiterAspect.class);

    private final ConcurrencyLimiterRegistry concurrencyLimiterRegistry;


    public ConcurrencyLimiterAspect(ConcurrencyLimiterRegistry concurrencyLimiterRegistry) {
        this.concurrencyLimiterRegistry = concurrencyLimiterRegistry;
    }

    @Pointcut(value = "@within(concurrencyLimiter) || @annotation(concurrencyLimiter)", argNames = "concurrencyLimiter")
    public void matchAnnotatedClassOrMethod(ConcurrencyLimiter concurrencyLimiter) {
    }





    @Around(value = "matchAnnotatedClassOrMethod(concurrencyLimiterAnnotation)", argNames = "proceedingJoinPoint, concurrencyLimiterAnnotation")
    public Object concurrencyLimiter(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        logger.debug("methodName {}", methodName);

        ConcurrencyLimiter  concurrencyLimiterAnnotation =  getConcurrencyLimiterAnnotation(proceedingJoinPoint);

        if (concurrencyLimiterAnnotation == null) { //because annotations wasn't found
            return proceedingJoinPoint.proceed();
        }
       String key = !concurrencyLimiterAnnotation.name().isEmpty() ? concurrencyLimiterAnnotation.name() : proceedingJoinPoint.getSignature().toShortString();
        Limiter<Void> limiter = concurrencyLimiterRegistry.getLimiter(key);
        logger.info(" limiter {}", limiter);
        Limiter.Listener listener = limiter.acquire(null).orElse(null);
        if (listener != null) {
            try {

                Object result = proceedingJoinPoint.proceed();
                listener.onSuccess();
                return result;
            } catch (Throwable throwable) {
                listener.onDropped();
                return invokeFallback(proceedingJoinPoint, concurrencyLimiterAnnotation.fallbackMethod());
            }
        } else {
            return invokeFallback(proceedingJoinPoint, concurrencyLimiterAnnotation.fallbackMethod());
        }
    }


    private ConcurrencyLimiter getConcurrencyLimiterAnnotation(ProceedingJoinPoint proceedingJoinPoint) {
        if (logger.isDebugEnabled()) {
            logger.debug("ConcurrencyLimiter parameter is null");
        }
        if (proceedingJoinPoint.getTarget() instanceof Proxy) {
            logger.debug(
                    "The ConcurrencyLimiter  is kept on a interface which is acting as a proxy");
            return AnnotationExtractor
                    .extractAnnotationFromProxy(proceedingJoinPoint.getTarget(), ConcurrencyLimiter.class);
        } else {
            return AnnotationExtractor
                    .extract(proceedingJoinPoint.getTarget().getClass(), ConcurrencyLimiter.class);
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