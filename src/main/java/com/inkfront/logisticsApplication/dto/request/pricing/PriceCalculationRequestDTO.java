package com.inkfront.logisticsApplication.dto.request.pricing;

import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceCalculationRequestDTO {

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @DecimalMin(value = "0.1", message = "Distance must be greater than zero")
    private double distanceKm;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private double weight;

    @DecimalMin(value = "0.0", message = "Volume cannot be negative")
    private double volume;

    @Builder.Default
    private boolean express = false;

    @Builder.Default
    private boolean night = false;
}