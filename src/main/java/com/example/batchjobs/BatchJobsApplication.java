package com.example.batchjobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BatchJobsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchJobsApplication.class, args);
    }
}
