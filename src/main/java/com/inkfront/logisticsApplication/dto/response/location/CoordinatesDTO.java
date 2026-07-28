package com.inkfront.logisticsApplication.dto.response.location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing geographic coordinates (latitude and longitude).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Geographic coordinates (latitude, longitude)")
public class CoordinatesDTO {

    @Schema(description = "Latitude in decimal degrees", example = "6.5244")
    private double latitude;

    @Schema(description = "Longitude in decimal degrees", example = "3.3792")
    private double longitude;
}