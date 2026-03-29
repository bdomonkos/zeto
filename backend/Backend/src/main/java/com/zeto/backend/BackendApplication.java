package com.zeto.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Zeto backend application.
 * Enables Spring Boot auto-configuration and component scanning.
 */
@SpringBootApplication
public class BackendApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
