package com.inkfront.logisticsApplication.dto.request.dispatch;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualAssignDispatchRequestDTO {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Driver ID is required")
    private String driverId;

    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;

    private Integer priority;
    private LocalDateTime scheduledTime;
    private String notes;
}