package com.inkfront.logisticsApplication.dto.request.order;

import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSearchRequestDTO {

    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page;
    private Integer size;
}