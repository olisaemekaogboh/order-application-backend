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
public class DashboardAnalyticsReportDTO {

    private ReportSummaryDTO summary;
    private Long totalOrders;
    private Double totalRevenue;
    private Long activeDrivers;
    private Long totalCustomers;
    private Double averageOrderValue;
    private Double revenueGrowth;
    private Map<String, Object> chartData; // flexible for frontend
    private LocalDate generatedAt;
}