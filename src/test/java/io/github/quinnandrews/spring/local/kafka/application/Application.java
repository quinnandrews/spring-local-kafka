package io.github.quinnandrews.spring.local.kafka.application;

import io.github.quinnandrews.spring.local.kafka.config.EnableLocalKafka;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableLocalKafka
@SpringBootApplication
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
