package com.demo.lasvegas.annotator.spelresolver.configure;
import com.demo.lasvegas.annotator.spelresolver.DefaultSpelResolver;
import com.demo.lasvegas.annotator.spelresolver.SpelResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;


@Configuration
public class SpelResolverConfiguration {
    @Bean
    public SpelResolver spelResolver(SpelExpressionParser spelExpressionParser, ParameterNameDiscoverer parameterNameDiscoverer, BeanFactory beanFactory) {
        return new DefaultSpelResolver(spelExpressionParser, parameterNameDiscoverer, beanFactory);
    }

    @Bean
    public SpelExpressionParser spelExpressionParser() {
        return new SpelExpressionParser();
    }

    @Bean
    public ParameterNameDiscoverer parameterNameDiscoverer() {
        return new StandardReflectionParameterNameDiscoverer();
    }
}