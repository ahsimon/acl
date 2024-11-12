package com.demo.lasvegas.annotator.fallback;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import com.demo.lasvegas.annotator.spelresolver.SpelResolver;

import java.lang.reflect.Method;

public class FallbackExecutor {


    private static final Logger logger = LoggerFactory.getLogger(FallbackExecutor.class);

    private final SpelResolver spelResolver;
    private final FallbackDecorators fallbackDecorators;

    public FallbackExecutor(SpelResolver spelResolver, FallbackDecorators fallbackDecorators) {
        this.spelResolver = spelResolver;
        this.fallbackDecorators = fallbackDecorators;
    }

    public Object invokeFallback(ProceedingJoinPoint proceedingJoinPoint, Method method, String fallbackMethodValue) throws Throwable {
        String fallbackMethodName = spelResolver.resolve(method, proceedingJoinPoint.getArgs(), fallbackMethodValue);

        FallbackMethod fallbackMethod = null;
        if (StringUtils.hasLength(fallbackMethodName)) {
            try {
                fallbackMethod = FallbackMethod
                        .create(fallbackMethodName, method, proceedingJoinPoint.getArgs(), proceedingJoinPoint.getTarget(), proceedingJoinPoint.getThis());
            } catch (NoSuchMethodException ex) {
                logger.warn("No fallback method match found", ex);
            }
        }
      return null;
    }
}
