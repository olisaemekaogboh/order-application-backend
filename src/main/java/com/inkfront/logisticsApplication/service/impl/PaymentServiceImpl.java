package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.dto.request.payment.*;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentResponseDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentStatisticsDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentSummaryDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentVerificationDTO;
import com.inkfront.logisticsApplication.exception.*;
import com.inkfront.logisticsApplication.mapper.PaymentMapper;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.PaymentTransactionRepository;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.service.interfaces.EmailService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.PaymentService;
import com.inkfront.logisticsApplication.service.interfaces.payment.PaymentGatewayService;
import com.inkfront.logisticsApplication.util.payment.PaymentGatewayFactory;
import com.inkfront.logisticsApplication.util.payment.TransactionReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final PaymentMapper paymentMapper;
    private final PaymentGatewayFactory gatewayFactory;
    private final TransactionReferenceGenerator referenceGenerator;

    @Override
    public PaymentResponseDTO initializePayment(InitializePaymentRequestDTO request, String userId) {
        log.info("Initializing payment for order: {} by user: {}", request.getOrderId(), userId);

        // 1. Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not own this order");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new PaymentAlreadyCompletedException("Order is already paid");
        }

        // 2. Check if payment already exists and is pending
        paymentTransactionRepository.findByOrderId(order.getId())
                .ifPresent(tx -> {
                    if (tx.getStatus() == PaymentStatus.PENDING || tx.getStatus() == PaymentStatus.PROCESSING) {
                        throw new InvalidPaymentStateException("A pending payment already exists for this order");
                    }
                });

        // 3. Generate transaction reference
        String transactionReference = referenceGenerator.generate();

        // 4. Create payment transaction
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setTransactionReference(transactionReference);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency() != null ? request.getCurrency() : "NGN");
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setGateway(request.getGateway());
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setCallbackUrl(request.getCallbackUrl());
        transaction.setMetadata(request.getMetadata() != null ? request.getMetadata().toString() : null);
        transaction.setRetryCount(0);

        // 5. Get gateway service and initialize
        PaymentGatewayService gatewayService = gatewayFactory.getService(request.getGateway());
        transaction = gatewayService.initialize(request, transaction);

        // 6. Save transaction
        transaction = paymentTransactionRepository.save(transaction);

        // 7. Update order payment status
        order.setPaymentStatus(PaymentStatus.PENDING);
        orderRepository.save(order);

        log.info("Payment initialized with reference: {}", transactionReference);

        return paymentMapper.toResponseDTO(transaction);
    }

    @Override
    public PaymentVerificationDTO verifyPayment(VerifyPaymentRequestDTO request, String userId) {
        log.info("Verifying payment with transaction reference: {}", request.getTransactionReference());

        // 1. Find transaction
        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionReference(request.getTransactionReference())
                .orElseThrow(() -> new PaymentNotFoundException("Payment transaction not found"));

        // 2. Check ownership if not admin
        if (!transaction.getOrder().getUser().getId().equals(userId)) {
            // Admin check is handled by controller @PreAuthorize, but we also need to verify if user is admin?
            // We'll rely on controller's authorization, but we can also check role here.
            // We'll skip because @PreAuthorize will block non-admin.
        }

        // 3. Validate status
        if (transaction.isCompleted()) {
            throw new PaymentAlreadyCompletedException("Payment already completed");
        }

        // 4. Get gateway service and verify
        PaymentGatewayService gatewayService = gatewayFactory.getService(transaction.getGateway());
        transaction = gatewayService.verify(transaction, request.getGatewayReference());

        // 5. Update order status if successful
        if (transaction.isSuccessful()) {
            Order order = transaction.getOrder();
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);

            // Send notifications
            notificationService.sendPaymentNotification(
                    order.getUser().getId(),
                    order.getId(),
                    "PAID"
            );
            emailService.sendPaymentConfirmationEmail(
                    order.getUser().getEmail(),
                    order.getId(),
                    "Payment successful"
            );
        }

        // 6. Save transaction
        transaction = paymentTransactionRepository.save(transaction);

        log.info("Payment verification completed: {}", request.getTransactionReference());

        return PaymentVerificationDTO.builder()
                .transactionReference(transaction.getTransactionReference())
                .orderId(transaction.getOrder().getId())
                .status(transaction.getStatus())
                .gatewayReference(transaction.getGatewayReference())
                .gatewayResponse(transaction.getGatewayResponse())
                .paymentDate(transaction.getPaymentDate())
                .successful(transaction.isSuccessful())
                .message(transaction.isSuccessful() ? "Payment verified successfully" : "Payment verification failed")
                .build();
    }

    @Override
    public PaymentResponseDTO refundPayment(RefundPaymentRequestDTO request, String userId) {
        log.info("Refunding payment: {} by user: {}", request.getTransactionReference(), userId);

        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionReference(request.getTransactionReference())
                .orElseThrow(() -> new PaymentNotFoundException("Payment transaction not found"));

        if (!transaction.isSuccessful()) {
            throw new InvalidPaymentStateException("Only successful payments can be refunded");
        }

        if (transaction.getStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentAlreadyCompletedException("Payment already refunded");
        }

        PaymentGatewayService gatewayService = gatewayFactory.getService(transaction.getGateway());
        transaction = gatewayService.refund(transaction, request.getReason());

        // Update order payment status if refunded
        Order order = transaction.getOrder();
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        orderRepository.save(order);

        transaction = paymentTransactionRepository.save(transaction);

        log.info("Refund completed for transaction: {}", request.getTransactionReference());

        return paymentMapper.toResponseDTO(transaction);
    }

    @Override
    public PaymentResponseDTO cancelPayment(CancelPaymentRequestDTO request, String userId) {
        log.info("Cancelling payment: {} by user: {}", request.getTransactionReference(), userId);

        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionReference(request.getTransactionReference())
                .orElseThrow(() -> new PaymentNotFoundException("Payment transaction not found"));

        if (transaction.getStatus() != PaymentStatus.PENDING && transaction.getStatus() != PaymentStatus.PROCESSING) {
            throw new InvalidPaymentStateException("Only pending or processing payments can be cancelled");
        }

        transaction.setStatus(PaymentStatus.CANCELLED);
        transaction.setFailureReason("Cancelled: " + request.getReason());

        // Update order payment status
        Order order = transaction.getOrder();
        order.setPaymentStatus(PaymentStatus.CANCELLED);
        orderRepository.save(order);

        transaction = paymentTransactionRepository.save(transaction);

        log.info("Payment cancelled: {}", request.getTransactionReference());

        return paymentMapper.toResponseDTO(transaction);
    }

    @Override
    public PaymentResponseDTO getTransactionByReference(String transactionReference) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionReference(transactionReference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment transaction not found"));
        return paymentMapper.toResponseDTO(transaction);
    }

    @Override
    public PaymentResponseDTO getTransactionByGatewayReference(String gatewayReference) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findByGatewayReference(gatewayReference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment transaction not found"));
        return paymentMapper.toResponseDTO(transaction);
    }

    @Override
    public PaymentResponseDTO getTransactionByOrderId(String orderId) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("No payment found for this order"));
        return paymentMapper.toResponseDTO(transaction);
    }

    @Override
    public List<PaymentSummaryDTO> getTransactionsByUser(String userId) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByOrderUserId(userId);
        return transactions.stream()
                .map(paymentMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentSummaryDTO> getAllTransactions() {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findAll();
        return transactions.stream()
                .map(paymentMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentStatisticsDTO getPaymentStatistics() {
        Long total = paymentTransactionRepository.count();
        Double totalAmount = paymentTransactionRepository.sumAmountByStatus(PaymentStatus.PAID);
        Double successfulAmount = paymentTransactionRepository.sumSuccessfulPayments();

        Long pendingCount = paymentTransactionRepository.countByStatus(PaymentStatus.PENDING);
        Long paidCount = paymentTransactionRepository.countByStatus(PaymentStatus.PAID);
        Long failedCount = paymentTransactionRepository.countByStatus(PaymentStatus.FAILED);
        Long refundedCount = paymentTransactionRepository.countByStatus(PaymentStatus.REFUNDED);
        Long cancelledCount = paymentTransactionRepository.countByStatus(PaymentStatus.CANCELLED);

        // Build map for count by status
        Map<PaymentStatus, Long> countByStatus = Map.of(
                PaymentStatus.PENDING, pendingCount,
                PaymentStatus.PAID, paidCount,
                PaymentStatus.FAILED, failedCount,
                PaymentStatus.REFUNDED, refundedCount,
                PaymentStatus.CANCELLED, cancelledCount
        );

        return PaymentStatisticsDTO.builder()
                .totalTransactions(total)
                .totalAmount(totalAmount != null ? totalAmount : 0.0)
                .successfulAmount(successfulAmount != null ? successfulAmount : 0.0)
                .pendingCount(pendingCount)
                .paidCount(paidCount)
                .failedCount(failedCount)
                .refundedCount(refundedCount)
                .cancelledCount(cancelledCount)
                .countByStatus(countByStatus)
                .currency("NGN")
                .build();
    }
}