package com.lasvegas.library.fallback.configure;


import com.lasvegas.library.fallback.VegasFallbackDecorator;
import com.lasvegas.library.fallback.VegasFallbackDecorators;
import com.lasvegas.library.fallback.VegasFallbackExecutor;
import com.lasvegas.library.spelresolver.SpelResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class VegasConcurrencyLimiterConfig {


    @Bean
    public VegasFallbackDecorators vegasFallbackDecorators(@Autowired List<VegasFallbackDecorator> fallbackDecorators) {
        return new VegasFallbackDecorators(fallbackDecorators);
    }
  @Bean

  public VegasFallbackExecutor vegasFallbackExecutor(SpelResolver spelResolver, VegasFallbackDecorators fallbackDecorators) {
   return new VegasFallbackExecutor(spelResolver, fallbackDecorators);
  }

 }
