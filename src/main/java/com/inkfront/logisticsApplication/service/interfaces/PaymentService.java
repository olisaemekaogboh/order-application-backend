package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.payment.*;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentResponseDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentStatisticsDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentSummaryDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentVerificationDTO;

import java.util.List;

public interface PaymentService {

    PaymentResponseDTO initializePayment(InitializePaymentRequestDTO request, String userId);

    PaymentVerificationDTO verifyPayment(VerifyPaymentRequestDTO request, String userId);

    PaymentResponseDTO refundPayment(RefundPaymentRequestDTO request, String userId);

    PaymentResponseDTO cancelPayment(CancelPaymentRequestDTO request, String userId);

    PaymentResponseDTO getTransactionByReference(String transactionReference);

    PaymentResponseDTO getTransactionByGatewayReference(String gatewayReference);

    PaymentResponseDTO getTransactionByOrderId(String orderId);

    List<PaymentSummaryDTO> getTransactionsByUser(String userId);

    List<PaymentSummaryDTO> getAllTransactions();

    PaymentStatisticsDTO getPaymentStatistics();
}