package com.inkfront.logisticsApplication.dto.request.vehicle;

import com.inkfront.logisticsApplication.domain.enums.MaintenanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleMaintenanceRequestDTO {

    private LocalDate scheduledDate;

    @NotBlank(message = "Maintenance type is required")
    private String type;

    private String description;

    @NotNull(message = "Status is required")
    private MaintenanceStatus status;

    private Double cost;
    private String serviceProvider;
    private Double odometerReading;
    private String notes;
}