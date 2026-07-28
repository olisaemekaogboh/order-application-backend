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
public class LiveTrackingDTO {

    private String trackingId;
    private String orderNumber;
    private TrackingStatus status;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double bearing;
    private Double accuracy;
    private LocalDateTime lastUpdate;
    private String driverName;
    private String driverPhone;
    private LocalDateTime estimatedArrival;
    private String etaText;
}