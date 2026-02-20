package com.example.itq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ItqApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItqApplication.class, args);
    }

}
