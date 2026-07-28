package com.inkfront.logisticsApplication.service.interfaces.payment;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.dto.request.payment.InitializePaymentRequestDTO;

public interface PaymentGatewayService {

    PaymentGateway getGateway();

    PaymentTransaction initialize(InitializePaymentRequestDTO request, PaymentTransaction transaction);

    PaymentTransaction verify(PaymentTransaction transaction, String gatewayReference);

    PaymentTransaction refund(PaymentTransaction transaction, String reason);

    boolean supports(PaymentGateway gateway);
}