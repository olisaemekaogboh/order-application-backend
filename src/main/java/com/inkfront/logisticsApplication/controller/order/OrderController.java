package com.inkfront.logisticsApplication.controller.order;

import com.inkfront.logisticsApplication.dto.request.order.OrderFilterRequestDTO;
import com.inkfront.logisticsApplication.dto.request.order.OrderRequestDTO;
import com.inkfront.logisticsApplication.dto.request.order.OrderUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.request.order.PriceCalculationRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderResponseDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderTrackingDTO;
import com.inkfront.logisticsApplication.dto.response.order.PriceCalculationResponseDTO;
import com.inkfront.logisticsApplication.service.interfaces.OrderService;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/calculate-price")
    @Operation(summary = "Calculate price for order")
    public ResponseEntity<ApiResponseDTO<PriceCalculationResponseDTO>> calculatePrice(
            @Valid @RequestBody PriceCalculationRequestDTO request) {
        log.info("Price calculation request for distance: {} km", request.getDistanceKm());
        PriceCalculationResponseDTO response = orderService.calculatePrice(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.PRICE_CALCULATED, response));
    }

    @PostMapping
    @Operation(summary = "Create new order")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> createOrder(
            @Valid @RequestBody OrderRequestDTO orderRequest,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("Create order request for user: {}", userId);
        OrderResponseDTO response = orderService.createOrder(orderRequest, userId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.ORDER_CREATED, response));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> getOrderById(@PathVariable String orderId) {
        log.info("Get order request for ID: {}", orderId);
        OrderResponseDTO response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> getOrderByNumber(@PathVariable String orderNumber) {
        log.info("Get order request for number: {}", orderNumber);
        OrderResponseDTO response = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get current user's orders")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<OrderResponseDTO>>> getUserOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        String userId = authentication.getName();
        log.info("Get user orders request for: {}", userId);

        OrderFilterRequestDTO filter = new OrderFilterRequestDTO();
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy != null ? sortBy : "createdAt");
        filter.setSortDirection(sortDirection);
        if (status != null) {
            filter.setStatus(com.inkfront.logisticsApplication.domain.enums.OrderStatus.valueOf(status));
        }

        PaginatedResponseDTO<OrderResponseDTO> response = orderService.getUserOrders(userId, filter);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all orders (Admin only)")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<OrderResponseDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Get all orders request");

        OrderFilterRequestDTO filter = new OrderFilterRequestDTO();
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy != null ? sortBy : "createdAt");
        filter.setSortDirection(sortDirection);
        if (status != null) {
            filter.setStatus(com.inkfront.logisticsApplication.domain.enums.OrderStatus.valueOf(status));
        }

        PaginatedResponseDTO<OrderResponseDTO> response = orderService.getAllOrders(filter);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody OrderUpdateRequestDTO updateRequest) {
        log.info("Update order status request for: {}", orderId);
        OrderResponseDTO response = orderService.updateOrderStatus(orderId, updateRequest);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.ORDER_STATUS_UPDATED, response));
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String reason) {
        log.info("Cancel order request for: {}", orderId);
        OrderResponseDTO response = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.ORDER_CANCELLED, response));
    }

    @GetMapping("/{orderId}/track")
    @Operation(summary = "Track order")
    public ResponseEntity<ApiResponseDTO<OrderTrackingDTO>> trackOrder(@PathVariable String orderId) {
        log.info("Track order request for: {}", orderId);
        OrderTrackingDTO response = orderService.trackOrder(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success("Order tracking retrieved", response));
    }

    @GetMapping("/my-orders/recent")
    @Operation(summary = "Get recent orders for current user")
    public ResponseEntity<ApiResponseDTO<List<OrderResponseDTO>>> getRecentOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit) {
        String userId = authentication.getName();
        log.info("Get recent orders request for: {}", userId);
        List<OrderResponseDTO> response = orderService.getRecentOrders(userId, limit);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/my-orders/count")
    @Operation(summary = "Get order count for current user")
    public ResponseEntity<ApiResponseDTO<Long>> getUserOrderCount(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Get order count request for: {}", userId);
        long count = orderService.countUserOrders(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("Order count retrieved", count));
    }

    @GetMapping("/my-orders/active-count")
    @Operation(summary = "Get active order count for current user")
    public ResponseEntity<ApiResponseDTO<Long>> getUserActiveOrderCount(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Get active order count request for: {}", userId);
        long count = orderService.countUserActiveOrders(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("Active order count retrieved", count));
    }

    @PutMapping("/{orderId}/assign-driver")
    @Operation(summary = "Assign driver to order (Admin only)")
    public ResponseEntity<ApiResponseDTO<Void>> assignDriver(
            @PathVariable String orderId,
            @RequestParam String driverId) {
        log.info("Assign driver request for order: {} to driver: {}", orderId, driverId);
        orderService.assignDriver(orderId, driverId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.ORDER_ASSIGNED, null));
    }

    @PutMapping("/{orderId}/payment-status")
    @Operation(summary = "Update payment status (Admin only)")
    public ResponseEntity<ApiResponseDTO<Void>> updatePaymentStatus(
            @PathVariable String orderId,
            @RequestParam String paymentStatus) {
        log.info("Update payment status request for order: {} to: {}", orderId, paymentStatus);
        orderService.updatePaymentStatus(orderId, paymentStatus);
        return ResponseEntity.ok(ApiResponseDTO.success("Payment status updated successfully", null));
    }
}