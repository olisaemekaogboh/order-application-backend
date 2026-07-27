// dto/request/order/OrderUpdateRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.order;

import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderUpdateRequestDTO {

    private String pickupLocation;
    private String deliveryLocation;
    private Double distanceKm;
    private Double weight;
    private Double volume;
    private String specialInstructions;
    private LocalDateTime pickupDate;
    private OrderStatus status;
    private String cancellationReason;
}