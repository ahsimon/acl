package com.demo.lasvegas.annotator;
import com.demo.lasvegas.annotator.utils.AnnotationExtractor;
import com.demo.lasvegas.controller.AnnotatedConcurrencyLimiterController;
import com.netflix.concurrency.limits.Limiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

@Aspect
@Component
public class VegasConcurrencyLimiterAspect {
    private static final Logger logger = LoggerFactory.getLogger(VegasConcurrencyLimiterAspect.class);

    @Pointcut(value = "@within(vegasConcurrencyLimiter) || @annotation(vegasConcurrencyLimiter)", argNames = "vegasConcurrencyLimiter")
    public void matchAnnotatedClassOrMethod(VegasConcurrencyLimiter vegasConcurrencyLimiter) {
    }


    @Around(value = "matchAnnotatedClassOrMethod(vegasConcurrencyLimiter)", argNames = "proceedingJoinPoint, vegasConcurrencyLimiter")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, VegasConcurrencyLimiter vegasConcurrencyLimiter) throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        logger.info ("methodName {} , class {}" , methodName,joinPoint.getTarget().getClass() );
       // VegasConcurrencyLimiter vegasConcurrencyLimiter = AnnotationExtractor.extract(joinPoint.getTarget().getClass(), VegasConcurrencyLimiter.class);
        logger.info ("vegasConcurrencyLimiter {} " , vegasConcurrencyLimiter);
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        logger.info ("{} executed in {} ms" , joinPoint.getSignature() , executionTime);
        return proceed;
    }
}