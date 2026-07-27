// dto/request/driver/DriverAssignmentRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.driver;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverAssignmentRequestDTO {

    @NotBlank(message = "Driver ID is required")
    private String driverId;

    @NotBlank(message = "Order ID is required")
    private String orderId;
}