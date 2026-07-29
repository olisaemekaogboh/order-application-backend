package com.inkfront.logisticsApplication.dto.request.vehicle;

import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleFilterRequestDTO {

    private String keyword;
    private VehicleStatus status;
    private VehicleType vehicleType;
    private String brand;
    private String model;
    private Integer year;
    private LocalDate insuranceExpiryBefore;
    private LocalDate inspectionDueBefore;
    private Boolean available;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}