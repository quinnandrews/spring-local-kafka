# Spring Local Kafka

## Description
Decorates the Testcontainers Kafka module with enhanced configuration for Spring Boot Applications.

Requires minimal configuration using Spring conventions, but a variety of optional properties are provided to override default behavior by profile, supporting local development in addition to test execution.

## Features
- Configure whether the Testcontainers Kafka module is active or not. Allows you to control its activation by profile.
- Configure the Docker Image to use with the Testcontainers Kafka module. Allows you to match the Kafka version used in local and test environments with the version in production.
- Configure the Testcontainers Kafka module to run with a fixed container name. Useful for local development so that developers can easily find the running container.
- Configure the Testcontainers Kafka module to run with a fixed port. Useful for local development so that developers can connect with consistent, predictable configuration.
- Configure whether to follow the Docker Container's log output. Useful for troubleshooting in some cases.

## Rationale
When developing an Application that uses Kafka in production, an instance of Kafka is needed for testing and local development, and it should be configurable in a way where it will spin up and tear down when the Application starts up and shuts down.

While Testcontainers is designed to provide that spin up/tear down capability to support Integration Tests, this project is designed to support running the Application locally as well, reducing the overhead that would come with maintaining Testcontainers in addition to some other solution that fundamentally does the same thing. There is no need to maintain a local Kafka server nor additional Docker configuration inside or outside the project.

## Requirements
### Java 17
https://adoptium.net/temurin/releases/?version=17

### Docker
https://www.docker.com/products/docker-desktop/ <br>
https://rancherdesktop.io/

NOTE: Rancher Desktop may not work correctly if Docker Desktop had been previously installed.

## Transitive Dependencies
- Spring Boot Starter Web 3.2.0
- Spring Boot Configuration Processor 3.2.0
- Spring Kafka 3.1.0
- Spring Boot Testcontainers 3.2.0
- Testcontainers Kafka 1.19.3

## Usage
### Configuration as a Test Dependency
While it is possible to declare `spring-local-kafka` as a compile dependency, and control its activation with profiles, it is better practice to declare it as a test dependency.

This means, however, that all configuration for `spring-local-kafka` (for both Integration Tests *and* for running the Application locally) can only reside in your project's test source. For Integration Tests this is common practice, but for running the Application locally it may seem unusual or perhaps difficult to do. However, by implementing the approach surfaced in this [article](https://bsideup.github.io/posts/local_development_with_testcontainers/) by Sergei Egorov, configuring a local profile in your project's test source becomes a simple process that will likely become a preferred practice as well.

### Add Spring Local Kafka
Add the `spring-local-kafka` artifact to your project as a test dependency:
```xml
<dependency>
    <groupId>io.github.quinnandrews</groupId>
    <artifactId>spring-local-kafka</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```
(NOTE: The `spring-local-kafka` artifact is NOT yet available in Maven Central, but is available from GitHub Packages, which requires additional configuration in your pom.xml file.)

### Configure a Local Profile
Create a properties files in your test resources directory to configuration for a `local` profile.

Configure the `local` profile with a fixed container name, so that developers can quickly and consistently identify the running container.

Configure the `local` profile with a fixed port, so that developers can connect with a consistent and predictable port.

Set other configuration properties as desired, or not at all to use default settings.

application-local.properties:
```properties
spring.local.kafka.container.image=confluentinc/cp-kafka:7.3.5
spring.local.kafka.container.name=local_kafka
spring.local.kafka.container.port=19200
```

In the example above, in addition to a fixed container name and port, the `local` profile has the following settings:
- The `confluentinc/cp-kafka:7.3.5` Docker Image is set to use a more recent version of Kafka than the default (`confluentinc/cp-kafka:5.4.3`) to match production.

### Implement a Spring Boot Application Class to Run the Application with the Local Profile
Add a Spring Boot Application Class named `LocalDevApplication` to your project's test source, preferably in the same package as the Spring Boot Application Class in the main source, to mirror the convention of Test Classes residing in the same package as the Classes they test.

Annotate `LocalDevApplication` with `@EnableLocalKafka` and `@Profile("local")`. The `@EnableLocalKafka` activates configuration of the Testcontainers Kafka module while `@Profile("local")` ensures that configuration declared within the `LocalDevApplication` is only scanned and initialized if the `local` profile is active.

Inside the body of the `main` method, instantiate an instance of `SpringApplication` with the Application Class residing in the main source, to ensure that configuration in the main source is scanned. Then activate the `local` profile programmatically by calling `setAdditionalProfiles`. This will allow you to run `LocalDevApplication` in IntelliJ IDEA by simply right-clicking on the Class in the Project Panel and selecting `Run 'LocalDevApplication'` without having to add the `local` profile to the generated Spring Boot Run Configuration.

LocalDevApplication.java:
```java
@EnableLocalKafka
@Profile("local")
@SpringBootApplication
public class LocalDevApplication {

    public static void main(final String[] args) {
        final var springApplication = new SpringApplication(Application.class);
        springApplication.setAdditionalProfiles("local");
        springApplication.run(args);
    }
}
```
### Configure a Test Profile

Configure the `test` profile to use a random container name and port by leaving their properties undeclared so that default settings will be used. Random names and ports are best practice for Integration Tests, and means that Integration Tests can be executed while the Application is running locally with the `local` profile.

Set other configuration properties as desired, or not at all to use default settings.

application-test.properties:
```properties
spring.local.kafka.container.image=confluentinc/cp-kafka:7.3.5
```
In the example above, the `test` profile has the following settings:
- The `confluentinc/cp-kafka:7.3.5` Docker Image is set to use a more recent version of Kafka than the default (`confluentinc/cp-kafka:5.4.3`) to match production.
-

#### Implement an Integration Test

Add an Integration Test Class. Annotate with `@EnableLocalKafka`, `@ActiveProfiles("test")` and `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`. The `@EnableLocalKafka` activates configuration of the Testcontainers Kafka module. The `@ActiveProfiles("test")` will activate the `test` profile when executed. And the `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`.

Write a test. The example below produces and consumes an Event using Kafka as the Broker.

Example:

```java
@EnableLocalKafka
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExampleTest {

    @Autowired(required = false)
    private GuitarPedalProducer guitarPedalProducer;

    @SpyBean
    private GuitarPedalConsumer guitarPedalConsumer;

    @Autowired(required = false)
    private GuitarPedalRepository guitarPedalRepository;

    @Test
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
```
NOTE: It is, of course, possible to declare `@EnableLocalKafka` in a Spring Boot Application Class, named `TestApplication`, for example, so that one does not have to add `@EnableLocalKafka` to every test Class, and that may be appropriate in some cases, but in general it is recommended that each test Class controls the declaration of the resources it needs. After all, some test Classes may need both Kafka and PostgreSQL, for instance, while other test Classes may only need one or the other. In such a case, initializing Kafka and PostgreSQL containers for all test Classes would waste resources and prolong the time it takes for test Classes to run.

## Supported Configuration Properties
**spring.local.kafka.engaged**<br/>
Whether the containerized Kafka server should be configured and started when the Application starts. By default, it is set to `true`. To disengage, set to `false`.

**spring.local.kafka.container.image**<br/>
The Docker Image with the chosen version of Kafka (example: `confluentinc/cp-kafka:7.3.5`). If undefined, a default will be used (`confluentinc/cp-kafka:5.4.3`).

**spring.local.kafka.container.name**<br/>
The name to use for the Docker Container when started. If undefined, a random name is used. Random names are preferred for Integration Tests, but when running the Application locally, a fixed name is useful, since it allows developers to find the running container with a consistent, predictable name.

**spring.local.kafka.container.port**<br/>
The port on the Docker Container to map with the Kafka port inside the container. If undefined, a random port is used. Random ports are preferred for Integration Tests, but when running the Application locally, a fixed port is useful, since it allows developers to configure any connecting, external tools or apps with a consistent, predictable port.

**spring.local.kafka.container.log.follow**<br/>
Whether the Application should log the output produced by the container's log. By default, container logs are not followed. Set with `true` to see their output.
