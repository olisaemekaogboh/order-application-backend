package com.inkfront.logisticsApplication.domain.entity;



import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "pricing_configs")
public class PricingConfig extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "base_rate_per_km", nullable = false)
    private Double baseRatePerKm;

    @Column(name = "minimum_charge", nullable = false)
    private Double minimumCharge;

    @Column(name = "weight_surcharge_per_kg")
    private Double weightSurchargePerKg = 0.0;

    @Column(name = "volume_surcharge_per_cubic_meter")
    private Double volumeSurchargePerCubicMeter = 0.0;

    @Column(name = "express_surcharge")
    private Double expressSurcharge = 0.0;

    @Column(name = "night_surcharge")
    private Double nightSurcharge = 0.0;

    @Column(name = "currency")
    private String currency = "NGN";

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "max_weight_kg")
    private Double maxWeightKg;

    @Column(name = "max_volume_cubic_meters")
    private Double maxVolumeCubicMeters;

    @Column(name = "max_distance_km")
    private Double maxDistanceKm;

    @Column(name = "description")
    private String description;

    public boolean isValid() {
        return active &&
                (effectiveFrom == null || LocalDateTime.now().isAfter(effectiveFrom)) &&
                (effectiveTo == null || LocalDateTime.now().isBefore(effectiveTo));
    }
}