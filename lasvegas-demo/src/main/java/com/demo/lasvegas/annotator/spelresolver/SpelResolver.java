package com.demo.lasvegas.annotator.spelresolver;

import java.lang.reflect.Method;

public interface SpelResolver {
    String resolve(Method method, Object[] arguments, String spelExpression);
}