package com.lasvegas.library.spelresolver;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;



import java.lang.reflect.Method;

public class DefaultSpelResolver implements EmbeddedValueResolverAware, SpelResolver {
    private static final String PLACEHOLDER_SPEL_REGEX = "^[$#]\\{.+}$";
    private static final String METHOD_SPEL_REGEX = "^#.+$";
    private static final String BEAN_SPEL_REGEX = "^@.+";

    private final SpelExpressionParser expressionParser;
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private final BeanFactory beanFactory;
    private StringValueResolver stringValueResolver;

    public DefaultSpelResolver(SpelExpressionParser spelExpressionParser, ParameterNameDiscoverer parameterNameDiscoverer, BeanFactory beanFactory) {
        this.expressionParser = spelExpressionParser;
        this.parameterNameDiscoverer = parameterNameDiscoverer;
        this.beanFactory = beanFactory;
    }

    @Override
    public String resolve(Method method, Object[] arguments, String spelExpression) {
        if (StringUtils.isEmpty(spelExpression)) {
            return spelExpression;
        }

        if (spelExpression.matches(PLACEHOLDER_SPEL_REGEX) && stringValueResolver != null) {
            return stringValueResolver.resolveStringValue(spelExpression);
        }

        if (spelExpression.matches(METHOD_SPEL_REGEX)) {
            SpelRootObject rootObject = new SpelRootObject(method, arguments);
            MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(rootObject, method, arguments, parameterNameDiscoverer);
            Object evaluated = expressionParser.parseExpression(spelExpression).getValue(evaluationContext);

            return (String) evaluated;
        }

        if (spelExpression.matches(BEAN_SPEL_REGEX)) {
            SpelRootObject rootObject = new SpelRootObject(method, arguments);

            MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(rootObject, method, arguments, parameterNameDiscoverer);
            evaluationContext.setBeanResolver(new BeanFactoryResolver(this.beanFactory));

            Object evaluated = expressionParser.parseExpression(spelExpression).getValue(evaluationContext);

            return (String) evaluated;
        }

        return spelExpression;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }
}