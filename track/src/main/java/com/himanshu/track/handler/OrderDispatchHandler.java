package com.himanshu.track.handler;

import com.himanshu.track.message.DispatchCompleted;
import com.himanshu.track.message.DispatchPreparing;
import com.himanshu.track.service.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * This class demostrates the use of @KafkaListener an @KafkaHandler
 * By keeping @KafkaListener at the class level, we can consume multiple event types from the same topic.
 * We just need to annotate each of the overloaded (with different event types) listener methods with @KafkaHandler.
 * If an unknown event is received from a topic, then the ListenerExecutionFailedException will be thrown
 * and polling will continue for the next message.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(
        id = "dispatchTrackingConsumerClient",
        topics = "order.tracked",
        groupId = "tracking.dispatch.tracking"
        //Below property to be paired with TrackingConfiguration class file
        //containerFactory = "kafkaListenerContainerFactory"
)
public class OrderDispatchHandler {

    private final OrderTrackingService trackingService;

    @KafkaHandler
    public void listen(DispatchPreparing payload) {
        log.info("Message received: " + payload);
        try {
            trackingService.process(payload);
        } catch (Exception e) {
            log.error("Processing failure", e);
        }
    }

    @KafkaHandler
    public void listen(DispatchCompleted payload) {
        log.info("Message received: " + payload);
        try {
            trackingService.process(payload);
        } catch (Exception e) {
            log.error("Processing failure", e);
        }
    }
}
