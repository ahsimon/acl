package com.lasvegas.library.fallback;

import com.lasvegas.library.spelresolver.SpelResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

public class VegasFallbackExecutor {

    private static final Logger logger = LoggerFactory.getLogger(VegasFallbackExecutor.class);

    private final SpelResolver spelResolver;


    public VegasFallbackExecutor(SpelResolver spelResolver) {
        this.spelResolver = spelResolver;
    }

    public Object execute(ProceedingJoinPoint proceedingJoinPoint, Method method, String fallbackMethodValue, Throwable throwable) throws Throwable {
        String fallbackMethodName = spelResolver.resolve(method, proceedingJoinPoint.getArgs(), fallbackMethodValue);

        VegasFallbackMethod fallbackMethod = null;
        if (StringUtils.hasLength(fallbackMethodName)) {
            try {
                fallbackMethod = VegasFallbackMethod
                        .create(fallbackMethodName, method, proceedingJoinPoint.getArgs(), proceedingJoinPoint.getTarget(), proceedingJoinPoint.getThis());
            } catch (NoSuchMethodException ex) {
                logger.warn("No fallback method match found", ex);
            }
        }
        // not fallback declared throws original exception
        if(fallbackMethod == null){
              throw throwable;
        }
      return fallbackMethod.fallback(throwable);
    }
}