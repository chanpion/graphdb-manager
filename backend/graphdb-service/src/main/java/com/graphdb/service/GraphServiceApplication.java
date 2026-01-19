package com.graphdb.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Service模块启动类
 */
@SpringBootApplication(scanBasePackages = "com.graphdb.service")
public class GraphServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GraphServiceApplication.class, args);
    }
}
