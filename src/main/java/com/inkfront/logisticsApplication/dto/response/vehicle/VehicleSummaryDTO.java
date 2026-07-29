package com.inkfront.logisticsApplication.dto.response.vehicle;

import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSummaryDTO {

    private String id;
    private String vehicleNumber;
    private String registrationNumber;
    private String brand;
    private String model;
    private Integer year;
    private VehicleType vehicleType;
    private VehicleStatus status;
    private String currentDriverName;
}