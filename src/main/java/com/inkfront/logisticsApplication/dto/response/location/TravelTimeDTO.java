package com.inkfront.logisticsApplication.dto.response.location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing estimated travel time for a given distance and vehicle type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Estimated travel time details")
public class TravelTimeDTO {

    @Schema(description = "Distance in kilometers", example = "12.5")
    private double distanceKm;

    @Schema(description = "Estimated travel time in minutes", example = "25")
    private long estimatedMinutes;

    @Schema(description = "Type of vehicle (e.g., CAR, BIKE, TRUCK)", example = "CAR")
    private String vehicleType;
}