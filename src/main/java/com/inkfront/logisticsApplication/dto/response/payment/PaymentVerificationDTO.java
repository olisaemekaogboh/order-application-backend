package com.inkfront.logisticsApplication.dto.response.payment;

import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVerificationDTO {

    private String transactionReference;
    private String orderId;
    private PaymentStatus status;
    private String gatewayReference;
    private String gatewayResponse;
    private LocalDateTime paymentDate;
    private boolean successful;
    private String message;
}