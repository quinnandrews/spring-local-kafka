package io.github.quinnandrews.spring.local.kafka.application.web.guitarpedals;

import io.github.quinnandrews.spring.local.kafka.application.config.KafkaConfig;
import io.github.quinnandrews.spring.local.kafka.application.data.guitarpedals.GuitarPedal;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class GuitarPedalProducer {

    private static final Logger logger = LoggerFactory.getLogger(GuitarPedalProducer.class);

    private final KafkaTemplate<Long, GuitarPedal> kafkaTemplate;

    @Autowired
    public GuitarPedalProducer(final KafkaTemplate<Long, GuitarPedal> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void guitarPedalPurchased(final GuitarPedal guitarPedal) {
        final var record = new ProducerRecord<>(
                KafkaConfig.PEDAL_PURCHASED_ON_REVERB_TOPIC,
                guitarPedal.getId(),
                guitarPedal
        );
        try {
            kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);
        } catch (final ExecutionException e) {
            logger.error("Error: ", e.getCause());
            throw new GuitarPedalProducerException("Could not produce Record.", e);
        } catch (final TimeoutException | InterruptedException e) {
            logger.error("Error: ", e);
            throw new GuitarPedalProducerException("Could not produce Record.", e);
        }
    }
}
