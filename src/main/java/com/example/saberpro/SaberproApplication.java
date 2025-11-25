package com.example.saberpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("proyecto.saberpro.model")
@EnableJpaRepositories("proyecto.saberpro.repository")
@ComponentScan({"com.example.saberpro", "proyecto.saberpro"})  // ← AGREGA ESTO
public class SaberproApplication {
    public static void main(String[] args) {
        SpringApplication.run(SaberproApplication.class, args);
    }
}