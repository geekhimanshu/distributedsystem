package com.himanshu.track.handler;

import com.himanshu.track.message.DispatchPreparing;
import com.himanshu.track.service.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderDispatchHandler {

    private final OrderTrackingService trackingService;
    @KafkaListener(
            id = "orderDispatchConsumerClient",
            topics = "order.tracked",
            groupId = "dispatch.order.dispatched.consumer"
            //Below property to be paired with TrackingConfiguration class file
            //containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(DispatchPreparing payload) {
        log.info("Message received: " + payload);
        try {
            trackingService.process(payload);
        } catch (Exception e) {
            log.error("Processing failure", e);
        }
    }
}
