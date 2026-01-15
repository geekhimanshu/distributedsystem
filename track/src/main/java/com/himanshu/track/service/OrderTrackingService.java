package com.himanshu.track.service;

import com.himanshu.track.message.DispatchPreparing;
import com.himanshu.track.message.TrackingStatusUpdated;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class OrderTrackingService {

    private static final String TRACKING_STATUS_TOPIC = "order.status";
    private final KafkaTemplate<String, Object> kafkaProducer;

    public void process(DispatchPreparing dispatchPreparing) throws ExecutionException, InterruptedException {
        TrackingStatusUpdated statusUpdated = TrackingStatusUpdated.builder()
                .orderId(dispatchPreparing.getOrderId())
                .status("DELIVERED").build();
        kafkaProducer.send(TRACKING_STATUS_TOPIC, statusUpdated).get();
    }
}
