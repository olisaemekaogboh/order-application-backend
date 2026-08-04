package com.inkfront.logisticsApplication.dto.request.payment;

import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitializePaymentRequestDTO {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    private String currency = "NGN";

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment gateway is required")
    private PaymentGateway gateway;

    private String callbackUrl;

    private Map<String, Object> metadata;
}