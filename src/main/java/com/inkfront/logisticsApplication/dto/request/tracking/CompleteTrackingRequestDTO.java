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
public class CompleteTrackingRequestDTO {

    @NotBlank(message = "Tracking ID is required")
    private String trackingId;

    private String completionNotes;
}