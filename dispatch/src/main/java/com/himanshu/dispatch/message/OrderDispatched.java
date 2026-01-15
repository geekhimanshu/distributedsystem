package com.himanshu.dispatch.message;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDispatched {

    UUID orderId;
}
