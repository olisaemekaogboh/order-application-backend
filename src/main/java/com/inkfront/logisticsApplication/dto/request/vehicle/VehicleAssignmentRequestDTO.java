package com.inkfront.logisticsApplication.dto.request.vehicle;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleAssignmentRequestDTO {

    @NotBlank(message = "Driver ID is required")
    private String driverId;

    private String assignmentReason;
    private String notes;
}