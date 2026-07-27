// dto/request/order/OrderRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.order;

import com.inkfront.logisticsApplication.domain.enums.PaymentMethod;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderRequestDTO {

    @NotBlank(message = "Pickup location is required")
    private String pickupLocation;

    private Double pickupLatitude;
    private Double pickupLongitude;

    @NotBlank(message = "Delivery location is required")
    private String deliveryLocation;

    private Double deliveryLatitude;
    private Double deliveryLongitude;

    @NotNull(message = "Distance is required")
    @Positive(message = "Distance must be positive")
    private Double distanceKm;

    @Positive(message = "Weight must be positive")
    private Double weight = 0.0;

    @Positive(message = "Volume must be positive")
    private Double volume = 0.0;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private boolean expressDelivery = false;

    private String specialInstructions;

    @NotNull(message = "Pickup date is required")
    private LocalDateTime pickupDate;

    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    private String deliveryAddressId;
}