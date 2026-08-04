package com.inkfront.logisticsApplication.controller.order;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.dto.request.order.*;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderResponseDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderTrackingDTO;
import com.inkfront.logisticsApplication.dto.response.order.PriceCalculationResponseDTO;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import com.inkfront.logisticsApplication.service.interfaces.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    // ======================== PRICE CALCULATION ========================

    @PostMapping("/calculate-price")
    @Operation(summary = "Calculate order price")
    public ResponseEntity<ApiResponseDTO<PriceCalculationResponseDTO>> calculatePrice(
            @Valid @RequestBody PriceCalculationRequestDTO request) {
        log.info("Calculate price request");
        PriceCalculationResponseDTO response = orderService.calculatePrice(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    // ======================== ORDER CRUD ========================

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> createOrder(
            Authentication authentication,
            @Valid @RequestBody OrderRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Create order request for user: {}", user.getId());
        OrderResponseDTO response = orderService.createOrder(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.ORDER_CREATED, response));
    }

    // ======================== FIXED ROUTES (must be before /{orderId}) ========================

    @GetMapping("/my-orders")
    @Operation(summary = "Get current user's orders (my orders)")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<OrderResponseDTO>>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Get my orders for user: {}", user.getId());

        OrderFilterRequestDTO filter = buildFilter(page, size, status, sortBy, sortDirection, startDate, endDate);
        PaginatedResponseDTO<OrderResponseDTO> response = orderService.getUserOrders(user.getId(), filter);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent orders for current user")
    public ResponseEntity<ApiResponseDTO<List<OrderResponseDTO>>> getRecentOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Get recent orders for user: {}", user.getId());
        List<OrderResponseDTO> response = orderService.getRecentOrders(user.getId(), limit);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/count")
    @Operation(summary = "Count current user's orders")
    public ResponseEntity<ApiResponseDTO<Long>> countUserOrders(
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        long count = orderService.countUserOrders(user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, count));
    }

    @GetMapping("/count-active")
    @Operation(summary = "Count current user's active orders")
    public ResponseEntity<ApiResponseDTO<Long>> countUserActiveOrders(
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        long count = orderService.countUserActiveOrders(user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, count));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> getOrderByNumber(
            Authentication authentication,
            @PathVariable String orderNumber) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Get order by number: {}", orderNumber);
        OrderResponseDTO response = orderService.getOrderByNumber(user.getId(), orderNumber);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    // ======================== VARIABLE ROUTE (must be last) ========================

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> getOrderById(
            Authentication authentication,
            @PathVariable String orderId) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Get order by ID: {}", orderId);
        OrderResponseDTO response = orderService.getOrderById(user.getId(), orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/{orderId}/track")
    @Operation(summary = "Track an order")
    public ResponseEntity<ApiResponseDTO<OrderTrackingDTO>> trackOrder(
            Authentication authentication,
            @PathVariable String orderId) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Track order: {}", orderId);
        OrderTrackingDTO response = orderService.trackOrder(user.getId(), orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    // ======================== FILTERED LISTS (for admin/driver) ========================

    @GetMapping("/user")
    @Operation(summary = "Get current user's orders with filters (alias for /my-orders)")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<OrderResponseDTO>>> getUserOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Get user orders for user: {}", user.getId());

        OrderFilterRequestDTO filter = buildFilter(page, size, status, sortBy, sortDirection, startDate, endDate);
        PaginatedResponseDTO<OrderResponseDTO> response = orderService.getUserOrders(user.getId(), filter);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/driver")
    @Operation(summary = "Get current driver's orders")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<OrderResponseDTO>>> getDriverOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Get driver orders for driver: {}", user.getId());

        OrderFilterRequestDTO filter = buildFilter(page, size, status, sortBy, sortDirection, startDate, endDate);
        PaginatedResponseDTO<OrderResponseDTO> response = orderService.getDriverOrders(user.getId(), filter);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all orders (admin only)")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<OrderResponseDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Get all orders request");

        OrderFilterRequestDTO filter = buildFilter(page, size, status, sortBy, sortDirection, startDate, endDate);
        PaginatedResponseDTO<OrderResponseDTO> response = orderService.getAllOrders(filter);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    // ======================== ORDER UPDATES (PUT) ========================

    @PutMapping("/{orderId}/assign-driver")
    @Operation(summary = "Assign a driver to an order")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> assignDriver(
            @PathVariable String orderId,
            @Valid @RequestBody DriverAssignmentRequestDTO request) {
        log.info("Assign driver to order: {}", orderId);
        OrderResponseDTO response = orderService.assignDriver(orderId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.ORDER_UPDATED, response));
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody OrderStatusUpdateRequestDTO request) {
        log.info("Update status for order: {}", orderId);
        OrderResponseDTO response = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.ORDER_UPDATED, response));
    }

    @PutMapping("/{orderId}/payment")
    @Operation(summary = "Update payment status")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> updatePaymentStatus(
            @PathVariable String orderId,
            @Valid @RequestBody PaymentStatusUpdateRequestDTO request) {
        log.info("Update payment status for order: {}", orderId);
        OrderResponseDTO response = orderService.updatePaymentStatus(orderId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.ORDER_UPDATED, response));
    }

    @PutMapping("/{orderId}/tracking")
    @Operation(summary = "Update tracking information")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> updateTracking(
            @PathVariable String orderId,
            @Valid @RequestBody TrackingUpdateRequestDTO request) {
        log.info("Update tracking for order: {}", orderId);
        OrderResponseDTO response = orderService.updateTracking(orderId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.ORDER_UPDATED, response));
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> cancelOrder(
            Authentication authentication,
            @PathVariable String orderId,
            @Valid @RequestBody CancelOrderRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Cancel order: {} for user: {}", orderId, user.getId());
        OrderResponseDTO response = orderService.cancelOrder(
                user.getId(),
                orderId,
                request.getCancellationReason()
        );
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.ORDER_CANCELLED, response));
    }

    // ======================== PRIVATE HELPER ========================

    private OrderFilterRequestDTO buildFilter(int page, int size, String status, String sortBy,
                                              String sortDirection, LocalDate startDate, LocalDate endDate) {
        OrderFilterRequestDTO filter = new OrderFilterRequestDTO();
        filter.setPage(page);
        filter.setSize(size);
        if (status != null && !status.isEmpty()) {
            filter.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        }
        filter.setSortBy(sortBy);
        filter.setSortDirection(sortDirection);
        if (startDate != null) {
            filter.setStartDate(startDate.atStartOfDay());
        }
        if (endDate != null) {
            filter.setEndDate(endDate.atTime(23, 59, 59));
        }
        return filter;
    }

}