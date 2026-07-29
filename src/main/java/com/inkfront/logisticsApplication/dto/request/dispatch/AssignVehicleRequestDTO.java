package com.inkfront.logisticsApplication.dto.request.dispatch;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignVehicleRequestDTO {

    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;
}