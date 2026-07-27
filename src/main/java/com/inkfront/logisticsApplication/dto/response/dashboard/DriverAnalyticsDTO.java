package com.inkfront.logisticsApplication.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverAnalyticsDTO {

    private Long totalDrivers;
    private Long availableDrivers;
    private Long busyDrivers;
    private Long offlineDrivers;
    private Double averageRating;
    private Long totalDeliveries;
    private Map<String, Double> performanceMetrics; // e.g., completion rate, avg delivery time
}