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
public class VerifyPaymentRequestDTO {

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;

    @NotBlank(message = "Gateway reference is required")
    private String gatewayReference;
}