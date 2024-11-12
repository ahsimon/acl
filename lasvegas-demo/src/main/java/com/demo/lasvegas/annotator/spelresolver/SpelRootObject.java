package com.demo.lasvegas.annotator.spelresolver;

import java.lang.reflect.Method;


public class SpelRootObject {
    private final String className;
    private final String methodName;
    private final Object[] args;

    public SpelRootObject(Method method, Object[] args) {
        this.className = method.getDeclaringClass().getName();
        this.methodName = method.getName();
        this.args = args;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }
}