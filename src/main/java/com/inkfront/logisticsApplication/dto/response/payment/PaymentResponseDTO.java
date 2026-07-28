package com.inkfront.logisticsApplication.dto.response.payment;

import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.domain.enums.PaymentMethod;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {

    private String id;
    private String transactionReference;
    private String orderId;
    private Double amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentGateway gateway;
    private PaymentStatus status;
    private String gatewayReference;
    private String authorizationUrl;
    private String accessCode;
    private String callbackUrl;
    private String paymentData;
    private Map<String, Object> metadata;
    private String failureReason;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}