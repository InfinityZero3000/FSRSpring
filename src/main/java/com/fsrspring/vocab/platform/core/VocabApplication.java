package com.fsrspring.vocab.platform.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.fsrspring.vocab")
@EnableScheduling
public class VocabApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocabApplication.class, args);
    }
}
