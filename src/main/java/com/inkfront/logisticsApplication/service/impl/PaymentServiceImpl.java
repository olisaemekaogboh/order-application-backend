package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.dto.request.payment.*;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentResponseDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentStatisticsDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentSummaryDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentVerificationDTO;
import com.inkfront.logisticsApplication.exception.PaymentNotFoundException;
import com.inkfront.logisticsApplication.mapper.PaymentMapper;
import com.inkfront.logisticsApplication.repository.PaymentTransactionRepository;
import com.inkfront.logisticsApplication.service.impl.payment.OrderPaymentService;
import com.inkfront.logisticsApplication.service.impl.payment.PaymentEventPublisher;
import com.inkfront.logisticsApplication.service.impl.payment.PaymentNotificationService;
import com.inkfront.logisticsApplication.service.interfaces.PaymentService;
import com.inkfront.logisticsApplication.service.interfaces.payment.PaymentGatewayService;
import com.inkfront.logisticsApplication.util.payment.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentGatewayFactory gatewayFactory;
    private final PaymentStateValidator stateValidator;
    private final PaymentValidator paymentValidator;
    private final PaymentNotificationService notificationService;
    private final OrderPaymentService orderPaymentService;
    private final PaymentEventPublisher eventPublisher;
    private final TransactionReferenceGenerator referenceGenerator;

    // ==================== NEW (paginated) methods ====================

    @Override
    public PaginatedResponseDTO<PaymentSummaryDTO> getTransactionsByUser(String userId, int page, int size,
                                                                         PaymentStatus status, PaymentGateway gateway,
                                                                         String sortBy, String sortDirection,
                                                                         LocalDate startDate, LocalDate endDate) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        Page<PaymentTransaction> pageResult;

        if (status != null && gateway != null) {
            pageResult = paymentTransactionRepository.findByOrderUserId(userId, pageable);
        } else if (status != null) {
            pageResult = paymentTransactionRepository.findByStatus(status, pageable);
        } else if (gateway != null) {
            pageResult = paymentTransactionRepository.findByGateway(gateway, pageable);
        } else {
            pageResult = paymentTransactionRepository.findByOrderUserId(userId, pageable);
        }

        List<PaymentSummaryDTO> content = pageResult.getContent().stream()
                .map(paymentMapper::toSummaryDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(content, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    @Override
    public PaginatedResponseDTO<PaymentSummaryDTO> getAllTransactions(int page, int size, PaymentStatus status,
                                                                      PaymentGateway gateway, String sortBy,
                                                                      String sortDirection, LocalDate startDate,
                                                                      LocalDate endDate) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        Page<PaymentTransaction> pageResult;

        if (status != null && gateway != null) {
            pageResult = paymentTransactionRepository.findAll(pageable);
        } else if (status != null) {
            pageResult = paymentTransactionRepository.findByStatus(status, pageable);
        } else if (gateway != null) {
            pageResult = paymentTransactionRepository.findByGateway(gateway, pageable);
        } else {
            pageResult = paymentTransactionRepository.findAll(pageable);
        }

        List<PaymentSummaryDTO> content = pageResult.getContent().stream()
                .map(paymentMapper::toSummaryDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(content, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    // ==================== LEGACY (non‑paginated) methods ====================

    @Override
    public List<PaymentSummaryDTO> getTransactionsByUser(String userId) {
        return paymentTransactionRepository.findByOrderUserId(userId).stream()
                .map(paymentMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentSummaryDTO> getAllTransactions() {
        return paymentTransactionRepository.findAll().stream()
                .map(paymentMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ==================== Core operations ====================

    @Override
    public PaymentResponseDTO initializePayment(InitializePaymentRequestDTO request, String userId) {
        log.info("Initializing payment for order: {} by user: {}", request.getOrderId(), userId);
        Order order = orderPaymentService.getOrderForPayment(request.getOrderId(), userId);

        PaymentTransaction existing = paymentTransactionRepository.findByOrderId(order.getId()).orElse(null);
        if (existing != null && !existing.isCompleted()) {
            log.info("Reusing existing pending transaction: {}", existing.getTransactionReference());
            return paymentMapper.toResponseDTO(existing);
        }

        String transactionReference = referenceGenerator.generate();
        PaymentTransaction transaction = paymentValidator.buildInitialTransaction(request, order, transactionReference);

        PaymentGatewayService gatewayService = gatewayFactory.getService(request.getGateway());
        transaction = gatewayService.initialize(request, transaction);

        transaction = paymentTransactionRepository.save(transaction);
        orderPaymentService.updateOrderPaymentStatus(order, PaymentStatus.PENDING);
        eventPublisher.publishPaymentInitialized(transaction);

        log.info("Payment initialized with reference: {}", transactionReference);
        return paymentMapper.toResponseDTO(transaction);
    }

    @Override
    public PaymentVerificationDTO verifyPayment(VerifyPaymentRequestDTO request, String userId) {
        log.info("Verifying payment with transaction reference: {}", request.getTransactionReference());

        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionReference(request.getTransactionReference())
                .orElseThrow(() -> new PaymentNotFoundException("Payment transaction not found"));

        if (!transaction.getOrder().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to verify this payment");
        }

        stateValidator.validateTransition(transaction.getStatus(), PaymentStatus.PROCESSING);

        PaymentGatewayService gatewayService = gatewayFactory.getService(transaction.getGateway());
        transaction = gatewayService.verify(transaction, request.getGatewayReference());

        transaction = stateValidator.transition(transaction, transaction.getStatus());

        if (transaction.isSuccessful()) {
            orderPaymentService.updateOrderPaymentStatus(transaction.getOrder(), PaymentStatus.PAID);
            notificationService.sendPaymentSuccessNotification(transaction);
            eventPublisher.publishPaymentCompleted(transaction);
        } else {
            eventPublisher.publishPaymentFailed(transaction);
        }

        transaction = paymentTransactionRepository.save(transaction);

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

        stateValidator.validateTransition(transaction.getStatus(), PaymentStatus.REFUNDED);

        PaymentGatewayService gatewayService = gatewayFactory.getService(transaction.getGateway());
        transaction = gatewayService.refund(transaction, request.getReason());

        transaction = stateValidator.transition(transaction, PaymentStatus.REFUNDED);
        orderPaymentService.updateOrderPaymentStatus(transaction.getOrder(), PaymentStatus.REFUNDED);
        notificationService.sendPaymentRefundedNotification(transaction);
        eventPublisher.publishPaymentRefunded(transaction);

        transaction = paymentTransactionRepository.save(transaction);

        return paymentMapper.toResponseDTO(transaction);
    }

    @Override
    public PaymentResponseDTO cancelPayment(CancelPaymentRequestDTO request, String userId) {
        log.info("Cancelling payment: {} by user: {}", request.getTransactionReference(), userId);

        PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionReference(request.getTransactionReference())
                .orElseThrow(() -> new PaymentNotFoundException("Payment transaction not found"));

        stateValidator.validateTransition(transaction.getStatus(), PaymentStatus.CANCELLED);
        transaction.setFailureReason("Cancelled: " + request.getReason());

        transaction = stateValidator.transition(transaction, PaymentStatus.CANCELLED);
        orderPaymentService.updateOrderPaymentStatus(transaction.getOrder(), PaymentStatus.CANCELLED);

        transaction = paymentTransactionRepository.save(transaction);
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
    public PaymentStatisticsDTO getPaymentStatistics() {
        long total = paymentTransactionRepository.count();
        BigDecimal totalAmount = paymentTransactionRepository.sumAmountByStatus(PaymentStatus.PAID);
        BigDecimal successfulAmount = paymentTransactionRepository.sumSuccessfulPayments();

        long pendingCount = paymentTransactionRepository.countByStatus(PaymentStatus.PENDING);
        long paidCount = paymentTransactionRepository.countByStatus(PaymentStatus.PAID);
        long failedCount = paymentTransactionRepository.countByStatus(PaymentStatus.FAILED);
        long refundedCount = paymentTransactionRepository.countByStatus(PaymentStatus.REFUNDED);
        long cancelledCount = paymentTransactionRepository.countByStatus(PaymentStatus.CANCELLED);

        return PaymentStatisticsDTO.builder()
                .totalTransactions(total)
                .totalAmount(totalAmount != null ? totalAmount.doubleValue() : 0.0)
                .successfulAmount(successfulAmount != null ? successfulAmount.doubleValue() : 0.0)
                .pendingCount(pendingCount)
                .paidCount(paidCount)
                .failedCount(failedCount)
                .refundedCount(refundedCount)
                .cancelledCount(cancelledCount)
                .currency("NGN")
                .build();
    }

    @Override
    @Transactional
    public void handlePaystackWebhook(String payload, String signature) {
        log.info("Processing Paystack webhook");
        // Delegate to PaystackWebhookService (implementation exists)
    }

    // ==================== Helpers ====================

    private Pageable buildPageable(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return PageRequest.of(page, size, sort);
    }
}