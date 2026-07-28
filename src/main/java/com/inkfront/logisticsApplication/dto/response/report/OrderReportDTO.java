package com.inkfront.logisticsApplication.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReportDTO {

    private ReportSummaryDTO summary;
    private Long totalOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Long pendingOrders;
    private Long inTransitOrders;
    private Long deliveredOrders;
    private Double averageDeliveryTimeMinutes;
    private Double averageDistanceKm;
    private String mostRequestedVehicle;
    private Map<LocalTime, Long> peakOrderHours;
    private Map<String, Long> ordersByStatus;
    private Map<String, Long> ordersByLocation;
    private LocalDate generatedAt;
}