package io.github.quinnandrews.spring.local.kafka.application.web.guitarpedals;

import io.github.quinnandrews.spring.local.kafka.application.config.KafkaConfig;
import io.github.quinnandrews.spring.local.kafka.application.data.guitarpedals.GuitarPedal;
import io.github.quinnandrews.spring.local.kafka.application.data.guitarpedals.repository.GuitarPedalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GuitarPedalConsumer {

    private final GuitarPedalRepository guitarPedalRepository;

    @Autowired
    public GuitarPedalConsumer(final GuitarPedalRepository guitarPedalRepository) {
        this.guitarPedalRepository = guitarPedalRepository;
    }

    @KafkaListener(groupId = "local-dev",
                   topics = KafkaConfig.PEDAL_PURCHASED_ON_REVERB_TOPIC,
                   containerFactory="pedalListenerContainerFactory")
    public void guitarPedalPurchased(final GuitarPedal pedal) {
        guitarPedalRepository.save(pedal);
    }
}
