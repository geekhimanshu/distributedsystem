package com.himanshu.dispatch.integration;

import com.himanshu.dispatch.DispatchConfiguration;
import com.himanshu.dispatch.message.DispatchCompleted;
import com.himanshu.dispatch.message.DispatchPreparing;
import com.himanshu.dispatch.message.OrderCreated;
import com.himanshu.dispatch.message.OrderDispatched;
import com.himanshu.dispatch.util.TestEventData;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest(classes = {DispatchConfiguration.class}) // loads beans in DispatchConfiguration
// Setting below classMode ensures spring context is not loaded between each test method
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test") //Sets active profile to test so that we can use application-test.properties
// @EmbeddedKafka annotation provides us embedded kafka server for testing. Setting controlledShutdown to
// true helps in maintaining clean env.
@EmbeddedKafka(controlledShutdown = true)
public class OrderDispatchIntegrationTest {

    private final static String ORDER_CREATED_TOPIC = "order.created";
    private final static String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private final static String DISPATCH_TRACKING_TOPIC = "order.tracked";

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private KafkaTestListener testListener;

    @Configuration
    static class TestConfig {

        @Bean
        public KafkaTestListener testListener() {
            return new KafkaTestListener();
        }
    }

    @KafkaListener(groupId = "kafkaIntegrationTest", topics = { DISPATCH_TRACKING_TOPIC, ORDER_DISPATCHED_TOPIC })
    public static class KafkaTestListener {
        AtomicInteger dispatchPreparingCounter = new AtomicInteger(0);
        AtomicInteger orderDispatchedCounter = new AtomicInteger(0);
        AtomicInteger orderCompletedCounter = new AtomicInteger(0);

        @KafkaHandler
        void receiveDispatchPreparing(@Header (KafkaHeaders.RECEIVED_KEY) String key, @Payload DispatchPreparing payload) {
            log.debug("Received dispatchPreparing: key: " + key + " -payload: " + payload);
            MatcherAssert.assertThat(key, notNullValue());
            MatcherAssert.assertThat(payload, notNullValue());
            dispatchPreparingCounter.incrementAndGet();
        }

        @KafkaHandler
        void receiveOrderDispatched(@Header (KafkaHeaders.RECEIVED_KEY) String key, @Payload OrderDispatched payload) {
            log.debug("Received OrderDispatched: key: " + key + " -payload: " + payload);
            MatcherAssert.assertThat(key, notNullValue());
            MatcherAssert.assertThat(payload, notNullValue());
            orderDispatchedCounter.incrementAndGet();
        }

        @KafkaHandler
        void receiveDispatchCompleted(@Header (KafkaHeaders.RECEIVED_KEY) String key, @Payload DispatchCompleted payload) {
            log.debug("Received Dispatch completed: key: " + key + " -payload: " + payload);
            MatcherAssert.assertThat(key, notNullValue());
            MatcherAssert.assertThat(payload, notNullValue());
            orderCompletedCounter.incrementAndGet();
        }
    }

    @BeforeEach
    public void setup(){
        testListener.orderDispatchedCounter.set(0);
        testListener.dispatchPreparingCounter.set(0);
        testListener.orderCompletedCounter.set(0);
        registry.getListenerContainers().stream()
                .forEach(container -> ContainerTestUtils.waitForAssignment(container,
                        container.getContainerProperties().getTopics().length * embeddedKafkaBroker.getPartitionsPerTopic()));
    }
    @Test
    public void testOrderDispatchFlow() throws ExecutionException, InterruptedException {
        String key = UUID.randomUUID().toString();
        OrderCreated orderCreated = TestEventData.buildOrderCreatedEvent(UUID.randomUUID(), "my-item");
        sendMessage(ORDER_CREATED_TOPIC, key, orderCreated);

        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(testListener.dispatchPreparingCounter::get, equalTo(1));
        await().atMost(1, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(testListener.orderDispatchedCounter::get, equalTo(1));
        await().atMost(1, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(testListener.orderCompletedCounter::get, equalTo(1));
    }

    private void sendMessage(String topic, String key, Object data) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(MessageBuilder.withPayload(data)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(KafkaHeaders.TOPIC, topic).build()).get();
    }
}
