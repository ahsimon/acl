package com.lasvegas.library.configuration;


import com.lasvegas.library.fallback.VegasFallbackExecutor;
import com.lasvegas.library.spelresolver.SpelResolver;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VegasConcurrencyLimiterConfig {

 @Bean
 @Qualifier("vegasFallbackExecutor")
 public VegasFallbackExecutor vegasFallbackExecutor(SpelResolver spelResolver){
    return new VegasFallbackExecutor(spelResolver);
  }
 }
