package com.lasvegas.library.controller;

import com.lasvegas.library.annotation.VegasConcurrencyLimiter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

    @GetMapping("/test")
    @VegasConcurrencyLimiter(name = "default", fallbackMethod = "fallbackResponse")
    public Mono<ResponseEntity<String>> processRequest() {
        long initTime = System.currentTimeMillis();


        return Mono.fromCallable(() -> {
            Thread.sleep(1000); // Simulate delay
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
