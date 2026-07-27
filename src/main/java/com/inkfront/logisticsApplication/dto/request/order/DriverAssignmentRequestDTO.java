package com.inkfront.logisticsApplication.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverAssignmentRequestDTO {

    @NotBlank(message = "Driver ID is required")
    private String driverId;
}