package com.sparta.javafeed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication()
public class JavafeedApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavafeedApplication.class, args);
    }

}