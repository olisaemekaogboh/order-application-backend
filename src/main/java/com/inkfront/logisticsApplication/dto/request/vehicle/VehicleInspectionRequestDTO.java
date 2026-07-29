package com.inkfront.logisticsApplication.dto.request.vehicle;

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
public class VehicleInspectionRequestDTO {

    @NotNull(message = "Inspection date is required")
    private LocalDate inspectionDate;

    private String inspectorName;

    @NotBlank(message = "Result is required")
    private String result; // PASS, FAIL, PENDING

    private String remarks;
    private LocalDate nextInspectionDate;
    private String certificateNumber;
    private boolean compliant = true;
}