package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.order.OrderFilterRequestDTO;
import com.inkfront.logisticsApplication.dto.request.order.OrderRequestDTO;
import com.inkfront.logisticsApplication.dto.request.order.OrderUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.request.order.PriceCalculationRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderResponseDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderTrackingDTO;
import com.inkfront.logisticsApplication.dto.response.order.PriceCalculationResponseDTO;

import java.util.List;

public interface OrderService {

    PriceCalculationResponseDTO calculatePrice(PriceCalculationRequestDTO request);

    OrderResponseDTO createOrder(OrderRequestDTO orderRequest, String userId);

    OrderResponseDTO getOrderById(String orderId);

    OrderResponseDTO getOrderByNumber(String orderNumber);

    PaginatedResponseDTO<OrderResponseDTO> getUserOrders(String userId, OrderFilterRequestDTO filter);

    PaginatedResponseDTO<OrderResponseDTO> getDriverOrders(String driverId, OrderFilterRequestDTO filter);

    PaginatedResponseDTO<OrderResponseDTO> getAllOrders(OrderFilterRequestDTO filter);

    OrderResponseDTO updateOrderStatus(String orderId, OrderUpdateRequestDTO updateRequest);

    OrderResponseDTO cancelOrder(String orderId, String cancellationReason);

    OrderTrackingDTO trackOrder(String orderId);

    List<OrderResponseDTO> getRecentOrders(String userId, int limit);

    long countUserOrders(String userId);

    long countUserActiveOrders(String userId);

    void assignDriver(String orderId, String driverId);

    void updatePaymentStatus(String orderId, String paymentStatus);
}