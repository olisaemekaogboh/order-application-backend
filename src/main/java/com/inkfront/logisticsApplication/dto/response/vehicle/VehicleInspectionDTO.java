package com.inkfront.logisticsApplication.dto.response.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleInspectionDTO {

    private String id;
    private String vehicleId;
    private String vehicleNumber;
    private LocalDate inspectionDate;
    private String inspectorName;
    private String result;
    private String remarks;
    private LocalDate nextInspectionDate;
    private String certificateNumber;
    private boolean compliant;
}