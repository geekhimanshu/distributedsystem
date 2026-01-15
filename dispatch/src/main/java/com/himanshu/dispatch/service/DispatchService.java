package com.himanshu.dispatch.service;

import com.himanshu.dispatch.message.DispatchPreparing;
import com.himanshu.dispatch.message.OrderCreated;
import com.himanshu.dispatch.message.OrderDispatched;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private static final String ORDER_TRACKED_TOPIC = "order.tracked";
    private final KafkaTemplate<String, Object> kafkaProducer;

    public void process(OrderCreated orderCreated) throws ExecutionException, InterruptedException {
        OrderDispatched orderDispatched = OrderDispatched.builder().orderId(orderCreated.getOrderId()).build();
        kafkaProducer.send(ORDER_DISPATCHED_TOPIC, orderDispatched).get();

        DispatchPreparing dispatchPreparing = DispatchPreparing.builder()
                .orderId(orderCreated.getOrderId()).build();
        kafkaProducer.send(ORDER_TRACKED_TOPIC, dispatchPreparing.getOrderId());
    }
}
