package com.inkfront.logisticsApplication.dto.response.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingTimelineDTO {

    private String trackingId;
    private String orderNumber;
    private List<TrackingEventDTO> events;
}