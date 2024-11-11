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
public class ConcurrencyLimitController {


        private final Limiter<Void> limiter; 
        AbstractLimit vegasLimit ; 


    public ConcurrencyLimitController() {
    // Initialize the VegasLimit for concurrency control
         vegasLimit = VegasLimit.newBuilder()
                                             .initialLimit(2).alpha(3).beta(6).maxConcurrency(100)
                                             .build();
        // Create a limiter instance

        // Create a limiter instance
        this.limiter = SimpleLimiter.newBuilder()
                              .limit(vegasLimit)
                              .build();
    }

    @GetMapping("/new")
    public Mono<ResponseEntity<String>> processRequest() throws InterruptedException {
        int sleep = (int)(Math.random()*1000); // Simulate delay
        return Mono.defer(() -> {
            Limiter.Listener listener = limiter.acquire(null).orElse(null);
             try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
      
                e.printStackTrace();
            }
            if (listener != null) {
                return processWithLimit(listener);
            } else {
                return fallbackMethod();
            }
        });
    }

    private Mono<ResponseEntity<String>> processWithLimit(Limiter.Listener listener) {
        try {
            // Simulate processing some work
            Thread.sleep(50); // Simulate delay

            listener.onSuccess();
            return Mono.just(ResponseEntity.ok("Request processed successfully " + vegasLimit.getLimit()));
        } catch (InterruptedException e) {
            listener.onDropped();
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                           .body("Request processing error " + vegasLimit.getLimit()));
        }
    }

    private Mono<ResponseEntity<String>> fallbackMethod() {
        // Fallback response when too many requests are received
        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                                       .body("Service is currently overloaded. Please try again later."));
    }
}