// dto/request/order/OrderFilterRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.order;

import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderFilterRequestDTO {

    private String userId;
    private String driverId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double minPrice;
    private Double maxPrice;
    private String searchTerm;
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}