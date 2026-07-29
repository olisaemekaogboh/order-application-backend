package com.inkfront.logisticsApplication.dto.request.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteTrackingRequestDTO {
    private String trackingId;
    private String completionNotes;
}