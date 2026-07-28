package com.inkfront.logisticsApplication.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryDTO {

    private String transactionReference;
    private String orderId;
    private Double amount;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
}