package com.lasvegas.library.spelresolver;

import java.lang.reflect.Method;

public interface SpelResolver {
    String resolve(Method method, Object[] arguments, String spelExpression);
}