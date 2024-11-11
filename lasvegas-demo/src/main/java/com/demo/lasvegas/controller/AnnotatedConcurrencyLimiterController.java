package com.demo.lasvegas.controller;

import com.lasvegas.library.annotation.ConcurrencyLimited;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import reactor.core.publisher.Mono;

@RestController
public class AnnotatedConcurrencyLimiterController {

    @GetMapping("/test")
    @ConcurrencyLimited(name = "processRequestLimiter", fallbackMethod = "fallbackResponse")
    public Mono<ResponseEntity<String>> processRequest() {
        return Mono.fromCallable(() -> {
            Thread.sleep(50); // Simulate delay
            return ResponseEntity.ok("Request processed successfully");
        });
    }

    public Mono<ResponseEntity<String>> fallbackResponse() {
        Mono<ResponseEntity<String>> just = Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Fallback: Service is overloaded. Please try again later."));
        return just;
    }
}
