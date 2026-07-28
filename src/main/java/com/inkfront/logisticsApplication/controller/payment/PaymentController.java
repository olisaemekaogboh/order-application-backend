package com.inkfront.logisticsApplication.controller.payment;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.dto.request.payment.*;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentResponseDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentStatisticsDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentSummaryDTO;
import com.inkfront.logisticsApplication.dto.response.payment.PaymentVerificationDTO;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import com.inkfront.logisticsApplication.service.interfaces.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Payment processing and transaction management")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initialize")
    @Operation(summary = "Initialize a new payment")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> initializePayment(
            Authentication authentication,
            @Valid @RequestBody InitializePaymentRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Initialize payment request for order: {} by user: {}", request.getOrderId(), user.getId());
        PaymentResponseDTO response = paymentService.initializePayment(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify a payment")
    public ResponseEntity<ApiResponseDTO<PaymentVerificationDTO>> verifyPayment(
            Authentication authentication,
            @Valid @RequestBody VerifyPaymentRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Verify payment request for transaction: {} by user: {}", request.getTransactionReference(), user.getId());
        PaymentVerificationDTO response = paymentService.verifyPayment(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PostMapping("/refund")
    @Operation(summary = "Refund a payment (admin only)")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> refundPayment(
            Authentication authentication,
            @Valid @RequestBody RefundPaymentRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Refund payment request for transaction: {} by user: {}", request.getTransactionReference(), user.getId());
        PaymentResponseDTO response = paymentService.refundPayment(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.PAYMENT_REFUNDED, response));
    }

    @PutMapping("/cancel")
    @Operation(summary = "Cancel a pending payment")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> cancelPayment(
            Authentication authentication,
            @Valid @RequestBody CancelPaymentRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Cancel payment request for transaction: {} by user: {}", request.getTransactionReference(), user.getId());
        PaymentResponseDTO response = paymentService.cancelPayment(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.PAYMENT_CANCELLED, response));
    }

    @GetMapping("/{transactionReference}")
    @Operation(summary = "Get payment by transaction reference")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> getPaymentByReference(
            @PathVariable String transactionReference) {
        log.info("Get payment by reference: {}", transactionReference);
        PaymentResponseDTO response = paymentService.getTransactionByReference(transactionReference);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/reference/{gatewayReference}")
    @Operation(summary = "Get payment by gateway reference")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> getPaymentByGatewayReference(
            @PathVariable String gatewayReference) {
        log.info("Get payment by gateway reference: {}", gatewayReference);
        PaymentResponseDTO response = paymentService.getTransactionByGatewayReference(gatewayReference);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> getPaymentByOrderId(
            @PathVariable String orderId) {
        log.info("Get payment by order ID: {}", orderId);
        PaymentResponseDTO response = paymentService.getTransactionByOrderId(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/user")
    @Operation(summary = "Get all payments for current user (paginated)")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<PaymentSummaryDTO>>> getUserPayments(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentGateway gateway,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Get paginated payments for user: {}", user.getId());
        PaginatedResponseDTO<PaymentSummaryDTO> response = paymentService.getTransactionsByUser(
                user.getId(), page, size, status, gateway, sortBy, sortDirection, startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all payments (admin only) with pagination and filters")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<PaymentSummaryDTO>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentGateway gateway,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get all payments (admin) with filters");
        PaginatedResponseDTO<PaymentSummaryDTO> response = paymentService.getAllTransactions(
                page, size, status, gateway, sortBy, sortDirection, startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get payment statistics (admin only)")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaymentStatisticsDTO>> getPaymentStatistics() {
        log.info("Get payment statistics (admin)");
        PaymentStatisticsDTO response = paymentService.getPaymentStatistics();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }
}