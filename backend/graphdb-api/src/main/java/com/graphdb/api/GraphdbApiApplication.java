package com.graphdb.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

/**
 * API模块启动类
 */
@SpringBootApplication(scanBasePackages = "com.graphdb")
@MapperScan("com.graphdb.storage.repository")
public class GraphdbApiApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GraphdbApiApplication.class, args);
    }
}
