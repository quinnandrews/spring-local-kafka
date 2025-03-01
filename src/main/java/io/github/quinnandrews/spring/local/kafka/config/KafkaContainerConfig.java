package io.github.quinnandrews.spring.local.kafka.config;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * <p> Initializes and configures a module from Testcontainers that runs
 * Kafka inside a Docker Container. Requires minimal configuration using
 * Spring conventions, but a variety of optional properties are supported
 * to override default behavior.
 *
 * <p> See the project README for configuration details.
 *
 * @author Quinn Andrews
 */
@ConditionalOnProperty(name="spring.local.kafka.engaged",
                       havingValue="true",
                       matchIfMissing = true)
@Configuration
public class KafkaContainerConfig {

    public static final String KAFKA_DEFAULT_IMAGE = "confluentinc/cp-kafka:5.4.3";

    private static final Logger logger = LoggerFactory.getLogger(KafkaContainerConfig.class);

    private final String containerImage;
    private final String containerName;
    private final Integer containerPort;
    private final Boolean followContainerLog;

    /**
     * Constructs an instance of this Configuration Class with the given properties.
     *
     * @param containerImage      The Docker Image to use as the Container (optional).
     * @param containerName       The name to use for the Docker Container when started.
     * @param containerPort       The port on the Container that should map to Kafka (optional).
     * @param followContainerLog  Whether to log the output produced by the Container's logs (optional).
     */
    public KafkaContainerConfig(@Value("${spring.local.kafka.container.image:#{null}}")
                                final String containerImage,
                                @Value("${spring.local.kafka.container.name:#{null}}")
                                final String containerName,
                                @Value("${spring.local.kafka.container.port:#{null}}")
                                final Integer containerPort,
                                @Value("${spring.local.kafka.container.log.follow:#{false}}")
                                final Boolean followContainerLog) {
        this.containerImage = containerImage;
        this.containerName = containerName;
        this.containerPort = containerPort;
        this.followContainerLog = followContainerLog;
    }

    /**
     * Returns a Testcontainers Bean that runs Kafka inside a Docker Container
     * with the given configuration.
     *
     * @return KafkaContainer
     */
    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        final var container = new KafkaContainer(
                DockerImageName.parse(Optional.ofNullable(containerImage)
                        .orElse(KAFKA_DEFAULT_IMAGE))
        );
        Optional.ofNullable(containerPort).ifPresent(cp ->
                container.withCreateContainerCmdModifier(cmd -> cmd
                        .withName(containerName)
                        .withHostConfig(
                                new HostConfig().withPortBindings(
                                        new PortBinding(
                                                Ports.Binding.bindPort(cp),
                                                new ExposedPort(KafkaContainer.KAFKA_PORT)),
                                        new PortBinding(
                                                Ports.Binding.empty(),
                                                new ExposedPort(9092)),
                                        new PortBinding(
                                                Ports.Binding.empty(),
                                                new ExposedPort(KafkaContainer.ZOOKEEPER_PORT))
                                ))));
        if (followContainerLog) {
            container.withLogConsumer(new Slf4jLogConsumer(logger));
        }
        container.start();
        logger.info(MessageFormat.format("""
                      
                      
                        *************************************************************************************
                        |+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|
                        
                            Running Kafka for development and testing.
                        
                            Container: {0}
                            Image: {1}
                            Port Mapping: {2}:{3}
                        
                            Kafka Bootstrap Server URL: {4}
                        
                        |+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|+|
                        *************************************************************************************
                        """,
                container.getContainerName(),
                container.getDockerImageName(),
                String.valueOf(container.getMappedPort(KafkaContainer.KAFKA_PORT)),
                String.valueOf(KafkaContainer.KAFKA_PORT),
                container.getBootstrapServers()));
        return container;
    }
}
