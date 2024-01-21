package io.github.quinnandrews.spring.local.kafka.application.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.*;
import io.github.quinnandrews.spring.local.kafka.application.data.guitarpedals.GuitarPedal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.lang.Nullable;
import org.testcontainers.containers.KafkaContainer;

import java.util.HashMap;
import java.util.Optional;

@Configuration
public class KafkaConfig {

    public static final String PEDAL_PURCHASED_ON_REVERB_TOPIC = "pedal-purchased-on-reverb";

    private final KafkaContainer kafkaContainer;
    private final String bootstrapServers;

    @Autowired
    public KafkaConfig(@Nullable
                       final KafkaContainer kafkaContainer,
                       @Value("${spring.kafka.bootstrap-servers:#{null}}")
                       final String bootstrapServers) {
        this.kafkaContainer = kafkaContainer;
        this.bootstrapServers = bootstrapServers;
    }

    @Bean
    public NewTopic pedalPurchasedOnReverbTopic() {
        return TopicBuilder.name(PEDAL_PURCHASED_ON_REVERB_TOPIC).build();
    }

    @Bean
    public KafkaTemplate<Long, GuitarPedal> pedalKafkaTemplate(final ProducerFactory<Long, GuitarPedal> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<Long, GuitarPedal> pedalProducerFactory() {
        final var props = new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<Long, GuitarPedal> pedalConsumerFactory() {
        final var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "local-dev");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props,
                new LongDeserializer(),
                new JsonDeserializer<>(GuitarPedal.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, GuitarPedal> pedalListenerContainerFactory() {
        final var factory = new ConcurrentKafkaListenerContainerFactory<Long, GuitarPedal>();
        factory.setConsumerFactory(pedalConsumerFactory());
        return factory;
    }

    private String getBootstrapServers() {
        final var optionalContainer  = Optional.ofNullable(kafkaContainer);
        if (optionalContainer.isPresent()) {
            return optionalContainer.get().getBootstrapServers();
        }
        return bootstrapServers;
    }
}
