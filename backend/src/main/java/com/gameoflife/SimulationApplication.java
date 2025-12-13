package com.gameoflife;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;



@SpringBootApplication
@ComponentScan(basePackages = {"com.gameoflife", "com.ai", "com.service", "com.model"})
@EntityScan(basePackages = "com.gameoflife")
@EnableJpaRepositories(basePackages = "com.gameoflife")
public class SimulationApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimulationApplication.class, args);
        System.out.println("=== Game of Life REST API Started ===");
        System.out.println("Access at: http://localhost:8080/api/simulation/status");
    }
}