package com.inkfront.logisticsApplication.dto.request.tracking;

import com.inkfront.logisticsApplication.domain.enums.TrackingStatus;
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
public class StatusUpdateRequestDTO {

    @NotBlank(message = "Tracking ID is required")
    private String trackingId;

    @NotNull(message = "Status is required")
    private TrackingStatus status;

    private String description;
    private Double latitude;
    private Double longitude;
}