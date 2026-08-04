package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.*;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.domain.enums.PaymentMethod;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.dto.request.order.*;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderResponseDTO;
import com.inkfront.logisticsApplication.dto.response.order.OrderTrackingDTO;
import com.inkfront.logisticsApplication.dto.response.order.PriceCalculationResponseDTO;
import com.inkfront.logisticsApplication.exception.BadRequestException;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.mapper.OrderMapper;
import com.inkfront.logisticsApplication.mapper.PriceCalculationMapper;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.PricingConfigRepository;
import com.inkfront.logisticsApplication.repository.PaymentTransactionRepository;
import com.inkfront.logisticsApplication.service.interfaces.OrderService;
import com.inkfront.logisticsApplication.service.interfaces.PricingService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.DistanceService;
import com.inkfront.logisticsApplication.util.OrderNumberGenerator;
import com.inkfront.logisticsApplication.domain.constants.AppConstants;
import com.inkfront.logisticsApplication.domain.constants.ErrorMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PricingConfigRepository pricingConfigRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderMapper orderMapper;
    private final PriceCalculationMapper priceCalculationMapper;
    private final PricingService pricingService;
    private final NotificationService notificationService;
    private final DistanceService distanceService;
    private final OrderNumberGenerator orderNumberGenerator;
    @Override
    public PriceCalculationResponseDTO calculatePrice(PriceCalculationRequestDTO request) {
        // ... keep existing implementation unchanged
        log.info("Calculating price for distance: {} km, vehicle: {}", request.getDistanceKm(), request.getVehicleType());

        if (request.getDistanceKm() < AppConstants.MINIMUM_DISTANCE_KM) {
            throw new BadRequestException("Distance must be at least " + AppConstants.MINIMUM_DISTANCE_KM + " km");
        }

        if (request.getDistanceKm() > AppConstants.MAXIMUM_DISTANCE_KM) {
            throw new BadRequestException("Distance cannot exceed " + AppConstants.MAXIMUM_DISTANCE_KM + " km");
        }

        PricingConfig config = pricingConfigRepository
                .findByVehicleTypeAndActiveTrue(request.getVehicleType())
                .orElseThrow(() -> new ResourceNotFoundException("Pricing configuration not found for vehicle type: " + request.getVehicleType()));

        double basePrice = request.getDistanceKm() * config.getBaseRatePerKm();
        double weightSurcharge = request.getWeight() * config.getWeightSurchargePerKg();
        double volumeSurcharge = request.getVolume() * config.getVolumeSurchargePerCubicMeter();
        double expressSurcharge = request.isExpressDelivery() ? config.getExpressSurcharge() : 0.0;
        double nightSurcharge = request.isNightDelivery() ? config.getNightSurcharge() : 0.0;

        double totalPrice = basePrice + weightSurcharge + volumeSurcharge + expressSurcharge + nightSurcharge;
        totalPrice = Math.max(totalPrice, config.getMinimumCharge());

        PriceCalculationResponseDTO response = priceCalculationMapper.toDTO(
                request,
                config.getBaseRatePerKm(),
                basePrice,
                weightSurcharge,
                volumeSurcharge,
                expressSurcharge,
                nightSurcharge,
                totalPrice,
                config.getMinimumCharge(),
                config.getCurrency()
        );

        return response;
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequest, String userId) {
        log.info("Creating order for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (orderRequest.getDistanceKm() < AppConstants.MINIMUM_DISTANCE_KM) {
            throw new BadRequestException("Distance must be at least " + AppConstants.MINIMUM_DISTANCE_KM + " km");
        }

        PricingConfig config = pricingConfigRepository
                .findByVehicleTypeAndActiveTrue(orderRequest.getVehicleType())
                .orElseThrow(() -> new ResourceNotFoundException("Pricing configuration not found"));

        PriceCalculationRequestDTO priceRequest = new PriceCalculationRequestDTO();
        priceRequest.setDistanceKm(orderRequest.getDistanceKm());
        priceRequest.setWeight(orderRequest.getWeight());
        priceRequest.setVolume(orderRequest.getVolume());
        priceRequest.setVehicleType(orderRequest.getVehicleType());
        priceRequest.setExpressDelivery(orderRequest.isExpressDelivery());

        PriceCalculationResponseDTO priceResponse = calculatePrice(priceRequest);

        Order order = orderMapper.toEntity(orderRequest);
        order.setOrderNumber(orderNumberGenerator.generateOrderNumber());
        order.setUser(user);
        order.setBasePrice(priceResponse.getBasePrice());
        order.setWeightSurcharge(priceResponse.getWeightSurcharge());
        order.setVolumeSurcharge(priceResponse.getVolumeSurcharge());
        order.setExpressSurcharge(priceResponse.getExpressSurcharge());
        order.setTotalPrice(priceResponse.getTotalPrice());
        order.setCurrency(priceResponse.getCurrency());
        order.setExpress(orderRequest.isExpressDelivery());
        order.setOrderDate(LocalDateTime.now());

        // Payment status defaults to PENDING
        // Status defaults to PENDING
        // No payment transaction is created here - that's the payment service's job

        long estimatedHours = distanceService.estimateTravelTime(
                orderRequest.getDistanceKm(),
                orderRequest.getVehicleType().name());
        order.setEstimatedDeliveryDate(orderRequest.getPickupDate().plusHours(estimatedHours));

        order = orderRepository.save(order);

        notificationService.sendOrderUpdateNotification(
                userId,
                order.getId(),
                "CREATED");

        log.info("Order created successfully with ID: {} and number: {}", order.getId(), order.getOrderNumber());
        return orderMapper.toDTO(order);
    }



    @Override
    public OrderResponseDTO getOrderById(
            String userId,
            String orderId) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.ORDER_NOT_FOUND));

        return orderMapper.toDTO(order);
    }

    @Override
    public OrderResponseDTO getOrderByNumber(
            String userId,
            String orderNumber) {

        Order order = orderRepository
                .findByOrderNumberAndUserId(orderNumber, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.ORDER_NOT_FOUND));

        return orderMapper.toDTO(order);
    }

    @Override
    public PaginatedResponseDTO<OrderResponseDTO> getUserOrders(String userId, OrderFilterRequestDTO filter) {
        Pageable pageable = createPageable(filter);
        Page<Order> orders;

        if (filter.getStatus() != null) {
            orders = orderRepository.findByUserIdAndStatus(userId, filter.getStatus(), pageable);
        } else {
            orders = orderRepository.findByUserId(userId, pageable);
        }

        return createPaginatedResponse(orders);
    }

    @Override
    public PaginatedResponseDTO<OrderResponseDTO> getDriverOrders(String driverId, OrderFilterRequestDTO filter) {
        Pageable pageable = createPageable(filter);
        Page<Order> orders = orderRepository.findByDriverId(driverId, pageable);
        return createPaginatedResponse(orders);
    }

    @Override
    public PaginatedResponseDTO<OrderResponseDTO> getAllOrders(OrderFilterRequestDTO filter) {
        Pageable pageable = createPageable(filter);
        Page<Order> orders;

        if (filter.getStatus() != null && filter.getStartDate() != null && filter.getEndDate() != null) {
            orders = orderRepository.findOrdersBetweenDatesAndStatus(
                    filter.getStartDate(),
                    filter.getEndDate(),
                    filter.getStatus(),
                    pageable
            );
        } else if (filter.getStartDate() != null && filter.getEndDate() != null) {
            // FIX: Convert List<Order> to Page<Order>
            List<Order> orderList = orderRepository.findOrdersBetweenDates(
                    filter.getStartDate(),
                    filter.getEndDate()
            );
            // Apply pagination manually
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), orderList.size());
            List<Order> pagedList = orderList.subList(start, end);
            orders = new PageImpl<>(pagedList, pageable, orderList.size());
        } else if (filter.getStatus() != null) {
            orders = orderRepository.findByStatus(filter.getStatus(), pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return createPaginatedResponse(orders);
    }

    @Override
    public OrderResponseDTO updateOrderStatus(
            String orderId,
            OrderStatusUpdateRequestDTO request) {

        log.info("Updating order {} to status {}", orderId, request.getStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.ORDER_NOT_FOUND));

        if (order.isDelivered()) {
            throw new BadRequestException(
                    ErrorMessages.ORDER_ALREADY_DELIVERED);
        }

        if (order.isCancelled()) {
            throw new BadRequestException(
                    ErrorMessages.ORDER_ALREADY_CANCELLED);
        }

        OrderStatus newStatus = request.getStatus();

        validateStatusTransition(
                order.getStatus(),
                newStatus
        );

        order.setStatus(newStatus);

        switch (newStatus) {

            case PICKED_UP -> {
                order.setPickupDate(LocalDateTime.now());
            }

            case DELIVERED -> {

                order.setDeliveryDate(LocalDateTime.now());

                if (order.getDriver() != null) {

                    Driver driver = order.getDriver();

                    driver.setAvailable(true);

                    driverRepository.save(driver);
                }
            }

            case CANCELLED -> {
                order.setCancelledAt(LocalDateTime.now());
                order.setCancellationReason(
                        request.getReason());

                if (order.getDriver() != null) {

                    Driver driver = order.getDriver();

                    driver.setAvailable(true);

                    driverRepository.save(driver);
                }
            }

            default -> {
                // no additional action
            }
        }

        order = orderRepository.save(order);

        notificationService.sendOrderUpdateNotification(
                order.getUser().getId(),
                order.getId(),
                newStatus.name());

        return orderMapper.toDTO(order);
    }

    @Override
    public OrderResponseDTO cancelOrder(
            String userId,
            String orderId,
            String cancellationReason) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.ORDER_NOT_FOUND));

        if (order.isDelivered()) {
            throw new BadRequestException(
                    ErrorMessages.ORDER_ALREADY_DELIVERED);
        }

        if (order.isCancelled()) {
            throw new BadRequestException(
                    ErrorMessages.ORDER_ALREADY_CANCELLED);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(cancellationReason);

        order = orderRepository.save(order);

        notificationService.sendOrderUpdateNotification(
                userId,
                orderId,
                "CANCELLED");

        return orderMapper.toDTO(order);
    }
    @Override
    public OrderTrackingDTO trackOrder(String userId, String orderId) {
        log.info("Tracking order: {} for user: {}", orderId, userId);

        // 1. Find the order
        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ORDER_NOT_FOUND));

        // 2. Convert to tracking DTO using mapper
        OrderTrackingDTO trackingDTO = orderMapper.toTrackingDTO(order);

        // 3. Populate tracking history
        List<OrderTrackingDTO.TrackingUpdateDTO> history = new java.util.ArrayList<>();

        try {
            // Add order creation event
            OrderTrackingDTO.TrackingUpdateDTO created = new OrderTrackingDTO.TrackingUpdateDTO();
            created.setStatus(order.getStatus());
            created.setStatusDisplayName(order.getStatus() != null ? order.getStatus().getDisplayName() : "Order Created");
            created.setTimestamp(order.getCreatedAt());
            created.setDescription("Order created");
            created.setLocation(order.getPickupLocation());
            history.add(created);

            // If driver is assigned, add assignment event
            if (order.getDriver() != null && order.getDriver().getId() != null) {
                OrderTrackingDTO.TrackingUpdateDTO assigned = new OrderTrackingDTO.TrackingUpdateDTO();
                assigned.setStatus(OrderStatus.ASSIGNED);
                assigned.setStatusDisplayName("Driver Assigned");
                assigned.setTimestamp(order.getUpdatedAt());
                assigned.setDescription("Driver " + order.getDriver().getName() + " assigned to your order");
                assigned.setLocation(order.getPickupLocation());
                history.add(assigned);
            }

            // If order has been picked up
            if (order.getPickupDate() != null) {
                OrderTrackingDTO.TrackingUpdateDTO pickedUp = new OrderTrackingDTO.TrackingUpdateDTO();
                pickedUp.setStatus(OrderStatus.PICKED_UP);
                pickedUp.setStatusDisplayName("Picked Up");
                pickedUp.setTimestamp(order.getPickupDate());
                pickedUp.setDescription("Package picked up from " + order.getPickupLocation());
                pickedUp.setLocation(order.getPickupLocation());
                history.add(pickedUp);
            }

            // If order is in transit
            if (order.getStatus() == OrderStatus.IN_TRANSIT) {
                OrderTrackingDTO.TrackingUpdateDTO inTransit = new OrderTrackingDTO.TrackingUpdateDTO();
                inTransit.setStatus(OrderStatus.IN_TRANSIT);
                inTransit.setStatusDisplayName("In Transit");
                inTransit.setTimestamp(order.getUpdatedAt());
                inTransit.setDescription("Package is on the way to " + order.getDeliveryLocation());
                inTransit.setLocation(order.getDeliveryLocation());
                history.add(inTransit);
            }

            // If order has been delivered
            if (order.getDeliveryDate() != null) {
                OrderTrackingDTO.TrackingUpdateDTO delivered = new OrderTrackingDTO.TrackingUpdateDTO();
                delivered.setStatus(OrderStatus.DELIVERED);
                delivered.setStatusDisplayName("Delivered");
                delivered.setTimestamp(order.getDeliveryDate());
                delivered.setDescription("Package delivered to " + order.getDeliveryLocation());
                delivered.setLocation(order.getDeliveryLocation());
                history.add(delivered);
            }

            // If order is cancelled
            if (order.getStatus() == OrderStatus.CANCELLED && order.getCancelledAt() != null) {
                OrderTrackingDTO.TrackingUpdateDTO cancelled = new OrderTrackingDTO.TrackingUpdateDTO();
                cancelled.setStatus(OrderStatus.CANCELLED);
                cancelled.setStatusDisplayName("Cancelled");
                cancelled.setTimestamp(order.getCancelledAt());
                cancelled.setDescription(order.getCancellationReason() != null ?
                        "Order cancelled: " + order.getCancellationReason() :
                        "Order cancelled");
                cancelled.setLocation(order.getPickupLocation());
                history.add(cancelled);
            }

        } catch (Exception e) {
            log.warn("Error building tracking history: {}", e.getMessage());
        }

        // Sort history by timestamp (oldest first)
        history.sort((a, b) -> {
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return a.getTimestamp().compareTo(b.getTimestamp());
        });

        trackingDTO.setTrackingHistory(history);

        log.info("Tracking data built for order: {}, history size: {}", orderId, history.size());
        return trackingDTO;
    }

    @Override
    public List<OrderResponseDTO> getRecentOrders(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Order> orders = orderRepository.findRecentOrdersByUser(userId, pageable);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countUserOrders(String userId) {
        return orderRepository.countByUserId(userId);
    }

    @Override
    public long countUserActiveOrders(String userId) {
        return orderRepository.countByUserIdAndStatusIn(
                userId,
                List.of(OrderStatus.PENDING, OrderStatus.ASSIGNED, OrderStatus.PICKED_UP, OrderStatus.IN_TRANSIT)
        );
    }

    @Override
    public OrderResponseDTO assignDriver(
            String orderId,
            DriverAssignmentRequestDTO request) {

        log.info("Assigning driver {} to order {}", request.getDriverId(), orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Only pending orders can be assigned.");
        }

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.DRIVER_NOT_FOUND));

        if (!Boolean.TRUE.equals(driver.getAvailable())) {
            throw new BadRequestException(
                    ErrorMessages.DRIVER_NOT_AVAILABLE);
        }

        order.setDriver(driver);
        order.setStatus(OrderStatus.ASSIGNED);

        orderRepository.save(order);

        driver.setAvailable(false);

        driverRepository.save(driver);

        notificationService.sendDriverAssignmentNotification(
                order.getUser().getId(),
                order.getId(),
                driver.getName());

        notificationService.sendDriverAssignmentNotification(
                driver.getId(),
                order.getId(),
                "You have been assigned to order "
                        + order.getOrderNumber());

        return orderMapper.toDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO updatePaymentStatus(String orderId, PaymentStatusUpdateRequestDTO request) {
        log.info("Updating payment status for order {} to {}", orderId, request.getPaymentStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ORDER_NOT_FOUND));

        PaymentStatus newStatus = request.getPaymentStatus();
        order.setPaymentStatus(newStatus);

        // ===== FIX: Update order status when payment becomes PAID =====
        if (newStatus == PaymentStatus.PAID) {
            // If order is still PENDING, update to PROCESSING
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.ASSIGNED);
                log.info("✅ Order {} status changed from PENDING to PROCESSING after payment update", orderId);
            }

            notificationService.sendPaymentNotification(
                    order.getUser().getId(),
                    order.getId(),
                    "PAID");
        }

        order = orderRepository.save(order);

        log.info("Payment status updated for order {} to {}", orderId, newStatus);
        return orderMapper.toDTO(order);
    }
    @Override
    public OrderResponseDTO updateTracking(
            String orderId,
            TrackingUpdateRequestDTO request) {

        log.info("Updating tracking for order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorMessages.ORDER_NOT_FOUND));

        // We'll just log the new coordinates
        log.debug("New tracking coordinates: lat={}, lon={}, location={}",
                request.getLatitude(), request.getLongitude(), request.getLocation());

        // Return the order DTO (unchanged)
        return orderMapper.toDTO(order);
    }

    private Pageable createPageable(OrderFilterRequestDTO filter) {
        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy());
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }

    private PaginatedResponseDTO<OrderResponseDTO> createPaginatedResponse(Page<Order> page) {
        List<OrderResponseDTO> content = page.getContent().stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Validate status transitions
        if (currentStatus == OrderStatus.PENDING && newStatus != OrderStatus.ASSIGNED && newStatus != OrderStatus.CANCELLED) {
            throw new BadRequestException("Pending orders can only be assigned or cancelled");
        }

        if (currentStatus == OrderStatus.ASSIGNED && newStatus != OrderStatus.PICKED_UP && newStatus != OrderStatus.CANCELLED) {
            throw new BadRequestException("Assigned orders can only be picked up or cancelled");
        }

        if (currentStatus == OrderStatus.PICKED_UP && newStatus != OrderStatus.IN_TRANSIT && newStatus != OrderStatus.CANCELLED) {
            throw new BadRequestException("Picked up orders can only be in transit or cancelled");
        }

        if (currentStatus == OrderStatus.IN_TRANSIT && newStatus != OrderStatus.DELIVERED && newStatus != OrderStatus.CANCELLED) {
            throw new BadRequestException("Orders in transit can only be delivered or cancelled");
        }

        if (currentStatus == OrderStatus.DELIVERED && newStatus != OrderStatus.DELIVERED) {
            throw new BadRequestException("Delivered orders cannot be changed");
        }

        if (currentStatus == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
            throw new BadRequestException("Cancelled orders cannot be changed");
        }
    }
}