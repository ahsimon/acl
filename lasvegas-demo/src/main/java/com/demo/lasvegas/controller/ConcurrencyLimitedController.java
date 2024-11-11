package com.demo.lasvegas.controller;

import com.netflix.concurrency.limits.Limiter;

import com.netflix.concurrency.limits.limit.AbstractLimit;
import com.netflix.concurrency.limits.limit.VegasLimit;
import com.netflix.concurrency.limits.limiter.SimpleLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ConcurrencyLimitedController {

    private final Limiter<Void> limiter; 
    AbstractLimit vegasLimit ; 

    public ConcurrencyLimitedController() {
        // Initialize the VegasLimit for concurrency control
         vegasLimit = VegasLimit.newBuilder()
         .initialLimit(100)
         .build();

        // Create a limiter instance
        this.limiter = SimpleLimiter.newBuilder()
                              .limit(vegasLimit)
                              .build();
    }

    @GetMapping("/process/simple")
    public Mono<ResponseEntity<String>> processRequest() {
        return Mono.defer(() -> {
            Limiter.Listener listener = limiter.acquire(null).orElse(null);
            if (listener != null) {
                try {
                    // Simulate processing some work
                    Thread.sleep(100); // Simulate delay
                    
                    listener.onSuccess();
                    return Mono.just(ResponseEntity.ok("Request processed successfully limit: " + vegasLimit.getLimit()));
                } catch (InterruptedException e) {
                    listener.onDropped();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                   .body("Request processing error"));
                }
            } else {
                return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                                               .body("Too many requests, try again later"));
            }
        });
    }
}