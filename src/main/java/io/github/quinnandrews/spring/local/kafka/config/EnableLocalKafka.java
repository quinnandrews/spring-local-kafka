package io.github.quinnandrews.spring.local.kafka.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Imports the necessary configuration.
 *
 * @author Quinn Andrews
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(KafkaContainerConfig.class)
public @interface EnableLocalKafka {
}
