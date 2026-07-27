package com.inkfront.logisticsApplication.dto.request.driver;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverAvailabilityRequestDTO {

    @NotNull(message = "Availability status is required")
    private Boolean available;

}