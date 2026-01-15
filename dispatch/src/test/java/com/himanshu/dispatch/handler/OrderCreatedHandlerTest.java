package com.himanshu.dispatch.handler;

import com.himanshu.dispatch.message.OrderCreated;
import com.himanshu.dispatch.service.DispatchService;
import com.himanshu.dispatch.util.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

class OrderCreatedHandlerTest {

    private OrderCreatedHandler orderCreatedHandler;
    private DispatchService dispatchServiceMock;

    @BeforeEach
    void setUp() {
        dispatchServiceMock = mock(DispatchService.class);
        orderCreatedHandler = new OrderCreatedHandler(dispatchServiceMock);
    }

    @Test
    void listen_Success() throws ExecutionException, InterruptedException {
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(UUID.randomUUID(), UUID.randomUUID().toString());
        orderCreatedHandler.listen(testEvent);
        verify(dispatchServiceMock, times(1)).process(testEvent);
    }

    @Test
    void listen_ServiceThrowsException() throws ExecutionException, InterruptedException {
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(UUID.randomUUID(), UUID.randomUUID().toString());
        doThrow(new RuntimeException("Service Failure")).when(dispatchServiceMock).process(testEvent);
        orderCreatedHandler.listen(testEvent);
        verify(dispatchServiceMock, times(1)).process(testEvent);
    }
}