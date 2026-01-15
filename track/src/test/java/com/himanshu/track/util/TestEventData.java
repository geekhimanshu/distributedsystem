package com.himanshu.track.util;

import com.himanshu.track.message.DispatchPreparing;

import java.util.UUID;

public class TestEventData {

    public static DispatchPreparing buildOrderCreatedEvent(UUID orderId) {
        return DispatchPreparing.builder().orderId(orderId).build();
    }
}
