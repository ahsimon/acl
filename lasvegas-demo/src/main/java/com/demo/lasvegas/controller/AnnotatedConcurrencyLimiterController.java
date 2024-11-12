package com.demo.lasvegas.controller;

import com.demo.lasvegas.annotator.VegasConcurrencyLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@RestController
public class AnnotatedConcurrencyLimiterController {
    private static final Logger logger = LoggerFactory.getLogger(AnnotatedConcurrencyLimiterController.class);


    @GetMapping("/test")
    @VegasConcurrencyLimiter
    public Mono<ResponseEntity<String>> processRequest() {
        long initTime = System.currentTimeMillis();


        return Mono.fromCallable(() -> {
            Thread.sleep(50); // Simulate delay
            long executionTime = System.currentTimeMillis() - initTime;

            return ResponseEntity.ok("Request processed ok!! in " + executionTime);
        });
    }


    public Mono<ResponseEntity<String>> fallbackResponse() {
        Mono<ResponseEntity<String>> just = Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Fallback: Service is overloaded. Please try again later."));
        return just;
    }
}
