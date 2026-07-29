package com.inkfront.logisticsApplication.dto.response.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleAnalyticsDTO {

    private Long totalVehicles;
    private Long available;
    private Long assigned;
    private Long inTransit;
    private Long underMaintenance;
    private Long outOfService;
    private Long retired;
    private Long dueForMaintenance;
    private Long dueForInspection;
    private Double averageMileage;
    private Double totalMaintenanceCost;
    private Double averageFuelConsumption;
    private Map<String, Long> vehiclesByType;
    private Map<String, Long> vehiclesByStatus;
    private double utilizationRate;
}