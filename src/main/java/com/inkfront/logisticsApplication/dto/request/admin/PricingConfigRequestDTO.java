// dto/request/admin/PricingConfigRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.admin;

import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PricingConfigRequestDTO {

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotNull(message = "Base rate is required")
    @Positive(message = "Base rate must be positive")
    private Double baseRatePerKm;

    @NotNull(message = "Minimum charge is required")
    @Positive(message = "Minimum charge must be positive")
    private Double minimumCharge;

    @Positive(message = "Weight surcharge must be positive")
    private Double weightSurchargePerKg = 0.0;

    @Positive(message = "Volume surcharge must be positive")
    private Double volumeSurchargePerCubicMeter = 0.0;

    @Positive(message = "Express surcharge must be positive")
    private Double expressSurcharge = 0.0;

    @Positive(message = "Night surcharge must be positive")
    private Double nightSurcharge = 0.0;

    private String currency = "NGN";
    private boolean active = true;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private Double maxWeightKg;
    private Double maxVolumeCubicMeters;
    private Double maxDistanceKm;
    private String description;
}