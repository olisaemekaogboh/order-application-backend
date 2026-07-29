package com.inkfront.logisticsApplication.dto.response.vehicle;

import com.inkfront.logisticsApplication.domain.enums.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleMaintenanceDTO {

    private String id;
    private String vehicleId;
    private String vehicleNumber;
    private LocalDate maintenanceDate;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private MaintenanceStatus status;
    private String type;
    private String description;
    private Double cost;
    private String serviceProvider;
    private Double odometerReading;
    private String notes;
}