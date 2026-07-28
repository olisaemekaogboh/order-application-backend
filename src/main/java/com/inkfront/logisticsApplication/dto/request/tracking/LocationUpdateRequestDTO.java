package com.inkfront.logisticsApplication.dto.request.tracking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationUpdateRequestDTO {

    @NotBlank(message = "Tracking ID is required")
    private String trackingId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double accuracy;
    private Double altitude;
    private Double bearing;
    private Double speed;
    private String provider;
    private Double batteryLevel;
    private String networkType;
}