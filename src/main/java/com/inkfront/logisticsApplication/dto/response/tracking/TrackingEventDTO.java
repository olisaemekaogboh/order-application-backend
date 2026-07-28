package com.inkfront.logisticsApplication.dto.response.tracking;

import com.inkfront.logisticsApplication.domain.enums.TrackingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingEventDTO {

    private String id;
    private String eventType;
    private TrackingStatus oldStatus;
    private TrackingStatus newStatus;
    private String description;
    private Double latitude;
    private Double longitude;
    private String performedBy;
    private LocalDateTime timestamp;
}