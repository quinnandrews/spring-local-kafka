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
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.KafkaContainer;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@DirtiesContext
@ActiveProfiles("custom")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = Application.class)
public class CustomKafkaContainerConfigTest {

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
    void container_initialized() {
        // given the application is initialized
        // and the 'custom' profile is active
        // then the container is initialized
        assertNotNull(kafkaContainer);
        assertTrue(kafkaContainer.isRunning());
        // and the container matches the 'custom' configuration
        assertEquals("confluentinc/cp-kafka:7.3.5", kafkaContainer.getDockerImageName());
        assertEquals(19093, kafkaContainer.getMappedPort(KafkaContainer.KAFKA_PORT));
        assertEquals("PLAINTEXT://127.0.0.1:19093", kafkaContainer.getBootstrapServers());
    }

    @Test
    @Order(2)
    void producerFactory_initialized() {
        // given the application is initialized
        // and the 'custom' profile is active
        // then the producerFactory is initialized
        assertNotNull(guitarPedalProducerFactory);
        // and the producerFactory matches the container
        assertEquals(
                kafkaContainer.getBootstrapServers(),
                guitarPedalProducerFactory.getConfigurationProperties()
                        .get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)
                        .toString());
    }

    @Test
    @Order(3)
    void consumerFactory_initialized() {
        // given the application is initialized
        // and the 'custom' profile is active
        // then the consumerFactory is initialized
        assertNotNull(guitarPedalConsumerFactory);
        // and the consumerFactory matches the container
        assertEquals(
                kafkaContainer.getBootstrapServers(),
                guitarPedalConsumerFactory.getConfigurationProperties()
                        .get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)
                        .toString());
    }

    @Test
    @Order(4)
    void producesAndConsumes() {
        // given the application is initialized
        // and the 'custom' profile is active
        // and the producer is initialized
        assertNotNull(guitarPedalProducer);
        // and the consumer is initialized
        assertNotNull(guitarPedalConsumer);
        // and the repository is initialized
        assertNotNull(guitarPedalRepository);
        // and there are 3 pedals in the database
        assertEquals(3L, guitarPedalRepository.count());
        // when a new pedal is purchased
        final var pedal = new GuitarPedal()
                .withId(4L)
                .withName("Pigtronix Quantum Time Modulator");
        // and a pedal purchased event is produced
        guitarPedalProducer.guitarPedalPurchased(pedal);
        await().pollInterval(Duration.ofSeconds(3))
                .atMost(10L, SECONDS)
                .untilAsserted(() -> {
                    // then the consumer receives the event
                    verify(guitarPedalConsumer, times(1)).guitarPedalPurchased(pedal);
                    // and the new pedal is added to the database
                    assertEquals(4L, guitarPedalRepository.count());
                    final var optionalPedal = guitarPedalRepository.findById(pedal.getId());
                    assertTrue(optionalPedal.isPresent());
                    assertEquals(pedal.getId(), optionalPedal.get().getId());
                    assertEquals(pedal.getName(), optionalPedal.get().getName());
                });
    }
}
