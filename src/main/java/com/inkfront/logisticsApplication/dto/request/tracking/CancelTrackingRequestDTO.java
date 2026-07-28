package com.inkfront.logisticsApplication.dto.request.tracking;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelTrackingRequestDTO {

    @NotBlank(message = "Tracking ID is required")
    private String trackingId;

    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}