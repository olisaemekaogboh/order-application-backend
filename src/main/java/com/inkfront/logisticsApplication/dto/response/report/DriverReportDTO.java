package com.inkfront.logisticsApplication.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverReportDTO {

    private ReportSummaryDTO summary;
    private Long totalDrivers;
    private Long availableDrivers;
    private Long busyDrivers;
    private Long offlineDrivers;
    private Long completedDeliveries;
    private Double acceptanceRate;
    private Double cancellationRate;
    private Double averageRating;
    private Double totalDistanceCoveredKm;
    private Double revenueGenerated;
    private Map<String, Long> driverPerformance; // driverId -> deliveries
    private LocalDate generatedAt;
}