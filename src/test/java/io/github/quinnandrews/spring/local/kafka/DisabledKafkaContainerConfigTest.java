package io.github.quinnandrews.spring.local.kafka;

import io.github.quinnandrews.spring.local.kafka.application.data.guitarpedals.repository.GuitarPedalRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.github.quinnandrews.spring.local.kafka.application.Application;
import io.github.quinnandrews.spring.local.kafka.application.data.guitarpedals.GuitarPedal;
import io.github.quinnandrews.spring.local.kafka.application.web.guitarpedals.GuitarPedalConsumer;
import io.github.quinnandrews.spring.local.kafka.application.web.guitarpedals.GuitarPedalProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.KafkaContainer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DirtiesContext
@ActiveProfiles("disabled")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = Application.class)
public class DisabledKafkaContainerConfigTest {

    @Autowired(required = false)
    private KafkaContainer kafkaContainer;

    @Autowired(required = false)
    private ProducerFactory<Long, GuitarPedal> guitarPedalProducerFactory;

    @Autowired(required = false)
    private ConsumerFactory<Long, GuitarPedal> guitarPedalConsumerFactory;

    @Autowired(required = false)
    private GuitarPedalProducer guitarPedalProducer;

    @SpyBean
    private GuitarPedalConsumer guitarPedalConsumer;

    @Autowired(required = false)
    private GuitarPedalRepository guitarPedalRepository;

    @Test
    @Order(1)
    void container_notInitialized() {
        // given the application is initialized
        // and the 'disabled' profile is active
        // then the container is not initialized
        assertNull(kafkaContainer);
    }

    @Test
    @Order(2)
    void producerFactory_initialized() {
        // given the application is initialized
        // and the 'disabled' profile is active
        // and the container is not initialized
        assertNull(kafkaContainer);
        // then the producerFactory is still initialized
        assertNotNull(guitarPedalProducerFactory);
        // but the producerFactory is initialized with port 9092 (Spring's default)
        assertEquals(
                "localhost:9092",
                guitarPedalProducerFactory.getConfigurationProperties()
                        .get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)
                        .toString()
        );
    }

    @Test
    @Order(3)
    void consumerFactory_initialized() {
        // given the application is initialized
        // and the 'disabled' profile is active
        // and the container is not initialized
        assertNull(kafkaContainer);
        // then the consumerFactory is still initialized
        assertNotNull(guitarPedalConsumerFactory);
        // but the consumerFactory is initialized with port 9092 (Spring's default)
        assertEquals(
                "localhost:9092",
                guitarPedalConsumerFactory.getConfigurationProperties()
                        .get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)
                        .toString()
        );
    }

    @Test
    @Order(4)
    void producesAndConsumes_throwsException_noConsumption() {
        // given the application is initialized
        // and the 'custom' profile is active
        // and the producer is initialized
        assertNotNull(guitarPedalProducer);
        // and the consumer is initialized
        assertNotNull(guitarPedalConsumer);
        // and the repository is initialized
        assertNotNull(guitarPedalRepository);
        // when a new pedal is purchased
        final var pedal = new GuitarPedal()
                .withId(6L)
                .withName("Boss SL-20 Slicer");
        // and a pedal purchased event is attempted
        // then an exception is thrown
        assertThrows(KafkaException.class,
                () -> guitarPedalProducer.guitarPedalPurchased(pedal));
        // and the consumer does not receive the event
        verify(guitarPedalConsumer, never()).guitarPedalPurchased(pedal);
        // and the new pedal has not been added to the database
        assertFalse(guitarPedalRepository.existsById(pedal.getId()));
    }
}
