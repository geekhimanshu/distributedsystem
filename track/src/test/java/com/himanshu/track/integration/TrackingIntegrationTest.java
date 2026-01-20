package com.himanshu.track.integration;

import com.himanshu.track.TrackApplication;
import com.himanshu.track.message.DispatchCompleted;
import com.himanshu.track.message.DispatchPreparing;
import com.himanshu.track.util.TestEventData;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest(classes = TrackApplication.class)
// Setting below classMode ensures spring context is not loaded between each test method
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test") //Sets active profile to test so that we can use application-test.properties
// @EmbeddedKafka annotation provides us embedded kafka server for testing. Setting controlledShutdown to
// true helps in maintaining clean env.
@EmbeddedKafka(controlledShutdown = true)
@Import(TrackingIntegrationTest.TestConfig.class)
public class TrackingIntegrationTest {

    private final static String DISPATCH_TRACKING_TOPIC = "order.tracked";

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private KafkaTestListener testListener;

    @TestConfiguration
    @EnableKafka
    static class TestConfig {

        @Bean
        public KafkaTestListener kafkaTestListener() {
            return new KafkaTestListener();
        }

        @Bean
        public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        public ProducerFactory<String, Object> producerFactory(@Value("${spring.embedded.kafka.brokers}") String bootstrapServers) {
            Map<String, Object> config = new HashMap<>();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new DefaultKafkaProducerFactory<>(config);
        }
    }

    @KafkaListener(groupId = "kafkaIntegrationTest", topics = DISPATCH_TRACKING_TOPIC)
    public static class KafkaTestListener {
        AtomicInteger orderTrackedCounter = new AtomicInteger(0);

        @KafkaHandler
        void receiveDispatchPreparing(@Payload DispatchPreparing payload) {
            log.info("Received DispatchPreparing: " + payload);
            orderTrackedCounter.incrementAndGet();
        }
    }

    @BeforeEach
    public void setup(){
        testListener.orderTrackedCounter.set(0);
        kafkaListenerEndpointRegistry.getListenerContainers().stream().forEach(container ->
                ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic()));
    }

    @Test
    public void testOrderTrackingFlow() throws ExecutionException, InterruptedException {
        DispatchPreparing dispatchPreparing = TestEventData.buildOrderCreatedEvent(UUID.randomUUID());
        sendMessage(DISPATCH_TRACKING_TOPIC, dispatchPreparing);

        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(testListener.orderTrackedCounter::get, equalTo(1));
    }

    private void sendMessage(String topic, Object data) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(MessageBuilder.withPayload(data).setHeader(KafkaHeaders.TOPIC, topic).build()).get();
    }
}
