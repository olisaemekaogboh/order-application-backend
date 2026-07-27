// dto/response/order/OrderResponseDTO.java
package com.inkfront.logisticsApplication.dto.response.order;

import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.PaymentMethod;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.dto.response.driver.DriverDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderResponseDTO {

    private String id;
    private String orderNumber;
    private String pickupLocation;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String deliveryLocation;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private Double distanceKm;
    private Double weight;
    private Double volume;
    private Double basePrice;
    private Double weightSurcharge;
    private Double volumeSurcharge;
    private Double expressSurcharge;
    private Double totalPrice;
    private String currency;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String paymentReference;
    private boolean isExpress;
    private String specialInstructions;
    private LocalDateTime orderDate;
    private LocalDateTime pickupDate;
    private LocalDateTime deliveryDate;
    private LocalDateTime estimatedDeliveryDate;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private String deliveryNotes;

    private UserDTO user;
    private DriverDTO driver;
    private String driverName;
    private String driverPhone;

    private String statusDisplayName;
    private Boolean isDelivered;
    private Boolean isCancelled;
    private Boolean isPending;
    private Boolean isPaid;
    private String estimatedDeliveryTime;
}