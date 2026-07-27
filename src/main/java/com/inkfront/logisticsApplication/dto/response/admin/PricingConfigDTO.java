// dto/response/admin/PricingConfigDTO.java
package com.inkfront.logisticsApplication.dto.response.admin;

import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PricingConfigDTO {

    private String id;
    private VehicleType vehicleType;
    private String vehicleTypeDisplay;
    private Double baseRatePerKm;
    private Double minimumCharge;
    private Double weightSurchargePerKg;
    private Double volumeSurchargePerCubicMeter;
    private Double expressSurcharge;
    private Double nightSurcharge;
    private String currency;
    private boolean active;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private Double maxWeightKg;
    private Double maxVolumeCubicMeters;
    private Double maxDistanceKm;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}