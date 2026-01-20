package com.himanshu.dispatch.service;

import com.himanshu.dispatch.message.DispatchCompleted;
import com.himanshu.dispatch.message.DispatchPreparing;
import com.himanshu.dispatch.message.OrderCreated;
import com.himanshu.dispatch.message.OrderDispatched;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private static final String ORDER_TRACKED_TOPIC = "order.tracked";
    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private final KafkaTemplate<String, Object> kafkaProducer;

    public void process(String key, OrderCreated orderCreated) throws ExecutionException, InterruptedException {
        OrderDispatched orderDispatched = OrderDispatched.builder()
                .orderId(orderCreated.getOrderId())
                .processedById(APPLICATION_ID)
                .notes("Dispatched: " + orderCreated.getItem())
                .build();
        //kafkaProducer.send(ORDER_DISPATCHED_TOPIC, orderDispatched).get();
        kafkaProducer.send(ORDER_DISPATCHED_TOPIC, key, orderDispatched).get();

        DispatchPreparing dispatchPreparing = DispatchPreparing.builder()
                .orderId(orderCreated.getOrderId())
                .build();
        //kafkaProducer.send(ORDER_TRACKED_TOPIC, dispatchPreparing).get();
        kafkaProducer.send(ORDER_TRACKED_TOPIC, key, dispatchPreparing).get();

        DispatchCompleted dispatchCompleted = DispatchCompleted.builder()
                .orderId(orderCreated.getOrderId())
                .date(LocalDate.now().toString())
                .build();
        kafkaProducer.send(ORDER_TRACKED_TOPIC, key, dispatchCompleted).get();
        log.info("Sent messages: key: " + key + " -orderId:" + orderCreated.getOrderId() + " - processedById: " + APPLICATION_ID);
    }
}
