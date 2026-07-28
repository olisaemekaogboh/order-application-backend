package com.inkfront.logisticsApplication.dto.request.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPaymentRequestDTO {

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;

    private String reason;
}