package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.order.*;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderResponseDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderTrackingDTO;
import com.inkfront.logisticsApplication.dto.response.order.PriceCalculationResponseDTO;

import java.util.List;

public interface OrderService {

    PriceCalculationResponseDTO calculatePrice(PriceCalculationRequestDTO request);

    OrderResponseDTO createOrder(OrderRequestDTO orderRequest, String userId);

    OrderResponseDTO getOrderById(String userId, String orderId);

    OrderResponseDTO getOrderByNumber(String userId, String orderNumber);

    PaginatedResponseDTO<OrderResponseDTO> getUserOrders(String userId, OrderFilterRequestDTO filter);

    PaginatedResponseDTO<OrderResponseDTO> getDriverOrders(String driverId, OrderFilterRequestDTO filter);

    PaginatedResponseDTO<OrderResponseDTO> getAllOrders(OrderFilterRequestDTO filter);

    // Updated methods: now using DTOs and returning OrderResponseDTO
    OrderResponseDTO updateOrderStatus(String orderId, OrderStatusUpdateRequestDTO request);

    OrderResponseDTO cancelOrder(String userId, String orderId, String cancellationReason);

    OrderTrackingDTO trackOrder(String userId, String orderId);

    List<OrderResponseDTO> getRecentOrders(String userId, int limit);

    long countUserOrders(String userId);

    long countUserActiveOrders(String userId);

    OrderResponseDTO assignDriver(String orderId, DriverAssignmentRequestDTO request);

    OrderResponseDTO updatePaymentStatus(String orderId, PaymentStatusUpdateRequestDTO request);

    OrderResponseDTO updateTracking(String orderId, TrackingUpdateRequestDTO request);
}