// dto/request/order/PriceCalculationRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.order;

import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PriceCalculationRequestDTO {

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

    private boolean nightDelivery = false;
}