package com.demo.lasvegas.annotator;
import com.demo.lasvegas.annotator.functions.CheckedSupplier;
import com.demo.lasvegas.annotator.spelresolver.SpelResolver;
import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.LimiterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;
import java.lang.reflect.Method;

@Aspect
@Component
public class VegasConcurrencyLimiterAspect {
    private static final Logger logger = LoggerFactory.getLogger(VegasConcurrencyLimiterAspect.class);
    private final SpelResolver spelResolver;
    private final VegasConcurrencyLimiterRegistry concurrencyLimiterRegistry;

    public VegasConcurrencyLimiterAspect(VegasConcurrencyLimiterRegistry concurrencyLimiterRegistry, SpelResolver spelResolver ) {
        this.concurrencyLimiterRegistry = concurrencyLimiterRegistry;
        this.spelResolver = spelResolver;
    }


    @Pointcut(value = "@within(vegasConcurrencyLimiter) || @annotation(vegasConcurrencyLimiter)", argNames = "vegasConcurrencyLimiter")
    public void matchAnnotatedClassOrMethod(VegasConcurrencyLimiter vegasConcurrencyLimiter) {
    }


    @Around(value = "matchAnnotatedClassOrMethod(vegasConcurrencyLimiter)", argNames = "proceedingJoinPoint, vegasConcurrencyLimiter")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, VegasConcurrencyLimiter vegasConcurrencyLimiter) throws Throwable {

        if( vegasConcurrencyLimiter == null){
            return joinPoint.proceed();
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        logger.info ("methodName {} , class {}" , methodName,joinPoint.getTarget().getClass() );
        logger.info ("vegasConcurrencyLimiter {} " , vegasConcurrencyLimiter);
        logger.info ("concurrencyLimiterRegistry {} " , concurrencyLimiterRegistry);

        String backend = spelResolver.resolve(method, joinPoint.getArgs(), vegasConcurrencyLimiter.name());

        logger.info ("backend {} " , backend);
        Limiter<Void> limiter =  concurrencyLimiterRegistry.getLimiter(backend);
        logger.info ("limiter {} " , backend);
        Limiter.Listener listener = limiter.acquire(null).orElse(null);

        return invokeFallback(joinPoint, vegasConcurrencyLimiter.fallbackMethod());
        /*if (listener != null) {
            try {

                Object result = joinPoint.proceed();
                listener.onSuccess();
                return result;
            } catch (Throwable throwable) {
                listener.onDropped();
                return invokeFallback(joinPoint, vegasConcurrencyLimiter.fallbackMethod());
            }
        } else {
            return invokeFallback(joinPoint, vegasConcurrencyLimiter.fallbackMethod());
        }*/
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