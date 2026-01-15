package com.himanshu.track.handler;

import com.himanshu.track.message.DispatchPreparing;
import com.himanshu.track.service.OrderTrackingService;
import com.himanshu.track.util.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class OrderDispatchHandlerTest {

    private OrderDispatchHandler orderDispatchHandler;
    private OrderTrackingService trackingService;

    @BeforeEach
    void setUp() {
        trackingService = mock(OrderTrackingService.class);
        orderDispatchHandler = new OrderDispatchHandler(trackingService);
    }

    @Test
    void listen_Success() throws ExecutionException, InterruptedException {
        DispatchPreparing testEvent = TestEventData.buildOrderCreatedEvent(UUID.randomUUID());
        orderDispatchHandler.listen(testEvent);
        verify(trackingService, times(1)).process(testEvent);
    }

    @Test
    void listen_ServiceThrowsException() throws ExecutionException, InterruptedException {
        DispatchPreparing testEvent = TestEventData.buildOrderCreatedEvent(UUID.randomUUID());
        doThrow(new RuntimeException("Service Failure")).when(trackingService).process(testEvent);
        orderDispatchHandler.listen(testEvent);
        verify(trackingService, times(1)).process(testEvent);
    }
}