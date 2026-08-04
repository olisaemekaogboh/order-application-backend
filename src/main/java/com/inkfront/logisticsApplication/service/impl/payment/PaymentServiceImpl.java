package com.inkfront.logisticsApplication.service.impl.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.dto.request.payment.*;
import com.inkfront.logisticsApplication.dto.request.tracking.StartTrackingRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentResponseDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentStatisticsDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentSummaryDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentVerificationDTO;
import com.inkfront.logisticsApplication.events.publisher.PaymentEventPublisher;
import com.inkfront.logisticsApplication.exception.BadRequestException;
import com.inkfront.logisticsApplication.exception.PaymentNotFoundException;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.mapper.PaymentMapper;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.PaymentTransactionRepository;
import com.inkfront.logisticsApplication.repository.tracking.TrackingSessionRepository;
import com.inkfront.logisticsApplication.service.interfaces.PaymentService;
import com.inkfront.logisticsApplication.service.impl.payment.flutterwave.FlutterwaveWebhookService;
import com.inkfront.logisticsApplication.service.impl.payment.gateway.PaymentGatewayFactory;
import com.inkfront.logisticsApplication.service.impl.payment.paystack.PaystackWebhookService;
import com.inkfront.logisticsApplication.service.interfaces.payment.PaymentGatewayService;
import com.inkfront.logisticsApplication.service.interfaces.tracking.TrackingService;
import com.inkfront.logisticsApplication.util.payment.TransactionReferenceGenerator;
import com.inkfront.logisticsApplication.validator.payment.PaymentStateValidator;
import com.inkfront.logisticsApplication.validator.payment.PaymentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final PaystackWebhookService paystackWebhookService;
    private final FlutterwaveWebhookService flutterwaveWebhookService;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final DriverRepository driverRepository;
    private final TrackingSessionRepository trackingSessionRepository;
    private final TrackingService trackingService;

    @Value("${payment.provider:paystack}")
    private String defaultProvider;

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("💳 PAYMENT SERVICE INITIALIZED");
        log.info("Default Provider from config: {}", defaultProvider);
        log.info("========================================");
    }

    // ==================== NEW (paginated) methods ====================

    @Override
    public PaginatedResponseDTO<PaymentSummaryDTO> getTransactionsByUser(String userId, int page, int size,
                                                                         PaymentStatus status, PaymentGateway gateway,
                                                                         String sortBy, String sortDirection,
                                                                         LocalDate startDate, LocalDate endDate) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        Page<PaymentTransaction> pageResult;

        if (status != null && gateway != null) {
            pageResult = paymentTransactionRepository.findByOrderUserIdAndStatusAndGateway(userId, status, gateway, pageable);
        } else if (status != null) {
            pageResult = paymentTransactionRepository.findByOrderUserIdAndStatus(userId, status, pageable);
        } else if (gateway != null) {
            pageResult = paymentTransactionRepository.findByOrderUserIdAndGateway(userId, gateway, pageable);
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
            pageResult = paymentTransactionRepository.findByStatusAndGateway(status, gateway, pageable);
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

        // Determine which gateway to use
        PaymentGateway gatewayToUse = determineGateway(request);
        request.setGateway(gatewayToUse);
        log.info("✅ Using payment gateway: {}", request.getGateway());

        // Load order from database to get amount
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        // Security check
        if (!order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to pay for this order");
        }

        // Check if amount is valid (from order)
        if (order.getTotalPrice() <= 0) {
            throw new BadRequestException("Order amount must be greater than zero");
        }

        // Check for existing transaction
        PaymentTransaction existing = paymentTransactionRepository.findByOrderId(order.getId()).orElse(null);
        if (existing != null && !existing.isCompleted()) {
            log.info("Reusing existing pending transaction: {}", existing.getTransactionReference());
            return paymentMapper.toResponseDTO(existing);
        }

        // Generate reference
        String transactionReference = referenceGenerator.generate();

        // Build transaction using validator - passes the order for amount
        PaymentTransaction transaction = paymentValidator.buildInitialTransaction(
                request,
                order,
                transactionReference
        );

        // Initialize with gateway
        PaymentGatewayService gatewayService = gatewayFactory.getService(request.getGateway());
        log.info("Gateway service class: {}", gatewayService.getClass().getSimpleName());
        transaction = gatewayService.initialize(request, transaction);

        // Save transaction
        transaction = paymentTransactionRepository.save(transaction);

        // Update order status
        order.setPaymentStatus(PaymentStatus.PENDING);
        orderRepository.save(order);

        eventPublisher.publishPaymentInitialized(transaction);

        log.info("✅ Payment initialized with reference: {} using gateway: {}",
                transactionReference, request.getGateway());
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

        // ✅ Check if already PAID - return success immediately
        if (transaction.getStatus() == PaymentStatus.PAID) {
            log.info("Transaction already PAID, returning success");
            return buildVerificationResponse(transaction, true, "Payment already verified");
        }

        // ✅ Only validate if not already PROCESSING
        if (transaction.getStatus() != PaymentStatus.PROCESSING) {
            stateValidator.validateTransition(transaction.getStatus(), PaymentStatus.PROCESSING);
        } else {
            log.info("Transaction already PROCESSING, proceeding with verification");
        }

        // Get the gateway-specific service and verify
        PaymentGatewayService gatewayService = gatewayFactory.getService(transaction.getGateway());
        transaction = gatewayService.verify(transaction, request.getGatewayReference());

        // ✅ Use transition with same-state check
        transaction = stateValidator.transition(transaction, transaction.getStatus());

        // Handle post-verification actions based on result
        if (transaction.isSuccessful()) {
            Order order = transaction.getOrder();

            // 1. Update payment status
            orderPaymentService.updateOrderPaymentStatus(order, PaymentStatus.PAID);

            // 2. Update order status from PENDING to ASSIGNED (since PROCESSING doesn't exist)
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.DISPATCH);
                orderRepository.save(order);
                log.info("✅ Order {} status updated from PENDING to ASSIGNED after payment", order.getId());
            }

            // 3. Send notifications
            notificationService.sendPaymentSuccessNotification(transaction);
            eventPublisher.publishPaymentCompleted(transaction);

            // 4. Auto-start tracking if driver available
            startTrackingAfterPayment(transaction, userId);

        } else {
            eventPublisher.publishPaymentFailed(transaction);
        }

        transaction = paymentTransactionRepository.save(transaction);

        return buildVerificationResponse(
                transaction,
                transaction.isSuccessful(),
                transaction.isSuccessful() ? "Payment verified successfully" : "Payment verification failed"
        );
    }

    /**
     * Helper method to start tracking after successful payment
     */
    private void startTrackingAfterPayment(PaymentTransaction transaction, String userId) {
        try {
            Order order = transaction.getOrder();

            // Check if tracking already exists
            if (trackingSessionRepository.findByOrderId(order.getId()).isPresent()) {
                log.info("Tracking already exists for order: {}", order.getId());
                return;
            }

            // Find available driver
            Driver availableDriver = driverRepository.findFirstByAvailableTrue().orElse(null);

            if (availableDriver != null) {
                StartTrackingRequestDTO trackingRequest = new StartTrackingRequestDTO();
                trackingRequest.setOrderId(order.getId());
                trackingRequest.setDriverId(availableDriver.getId());

                trackingService.startTracking(trackingRequest, userId);
                log.info("✅ Tracking started for order: {} with driver: {}",
                        order.getId(), availableDriver.getName());
            } else {
                log.warn("⚠️ No driver available for order: {}. Order is in ASSIGNED state waiting for driver assignment.",
                        order.getId());
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not start tracking for order {}: {}", transaction.getOrder().getId(), e.getMessage());
        }
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

    // ==================== Webhook handlers ====================

    @Override
    @Transactional
    public void handlePaystackWebhook(String payload, String signature) {
        log.info("Processing Paystack webhook");
        paystackWebhookService.processWebhook(payload, signature);
    }

    @Override
    @Transactional
    public void handleFlutterwaveWebhook(String payload, String signature) {
        log.info("Processing Flutterwave webhook");
        flutterwaveWebhookService.processWebhook(payload, signature);
    }

    // ==================== Helper Methods ====================

    /**
     * Determines which payment gateway to use based on:
     * 1. Request's specified gateway (if provided)
     * 2. Default provider from configuration
     * 3. Fallback to PAYSTACK if default is invalid
     */
    private PaymentGateway determineGateway(InitializePaymentRequestDTO request) {
        if (request.getGateway() != null) {
            log.info("Using gateway specified in request: {}", request.getGateway());
            return request.getGateway();
        }

        try {
            PaymentGateway gateway = PaymentGateway.valueOf(defaultProvider.toUpperCase());
            log.info("Using default provider from config: {}", gateway);
            return gateway;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid default provider: {}, falling back to PAYSTACK", defaultProvider);
            return PaymentGateway.PAYSTACK;
        }
    }

    /**
     * Builds a verification response DTO
     */
    private PaymentVerificationDTO buildVerificationResponse(PaymentTransaction transaction,
                                                             boolean successful,
                                                             String message) {
        return PaymentVerificationDTO.builder()
                .transactionReference(transaction.getTransactionReference())
                .orderId(transaction.getOrder().getId())
                .status(transaction.getStatus())
                .gatewayReference(transaction.getGatewayReference())
                .gatewayResponse(transaction.getGatewayResponse())
                .paymentDate(transaction.getPaymentDate())
                .successful(successful)
                .message(message)
                .build();
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return PageRequest.of(page, size, sort);
    }
}