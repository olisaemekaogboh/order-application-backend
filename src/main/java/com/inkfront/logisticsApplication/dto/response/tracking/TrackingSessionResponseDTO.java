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
public class TrackingSessionResponseDTO {

    private String id;
    private String orderId;
    private String orderNumber;
    private String driverId;
    private String driverName;
    private TrackingStatus status;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime lastUpdateTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime estimatedArrival;
    private LocalDateTime actualArrival;
    private Double distanceTraveledKm;
    private Integer routeDeviationCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}