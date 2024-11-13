package com.demo.lasvegas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.demo.lasvegas"})
public class LasVegasDemo {

    public static void main(String[] args) {

        SpringApplication.run(LasVegasDemo.class, args);
    }

}