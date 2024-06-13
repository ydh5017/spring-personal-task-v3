package com.sparta.javafeed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class JavafeedApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavafeedApplication.class, args);
    }

}