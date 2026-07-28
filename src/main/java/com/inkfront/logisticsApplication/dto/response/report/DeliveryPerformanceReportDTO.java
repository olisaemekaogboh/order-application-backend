package com.inkfront.logisticsApplication.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPerformanceReportDTO {

    private ReportSummaryDTO summary;
    private Duration averageDeliveryTime;
    private Duration averagePickupTime;
    private Double averageDistanceKm;
    private Duration fastestDelivery;
    private Duration longestDelivery;
    private Long delayedDeliveries;
    private Double onTimeDeliveryPercentage;
    private Map<String, Duration> deliveryTimesByDriver;
    private LocalDate generatedAt;
}