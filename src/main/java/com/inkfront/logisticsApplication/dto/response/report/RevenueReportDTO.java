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
public class RevenueReportDTO {

    private ReportSummaryDTO summary;
    private Double totalRevenue;
    private Double completedRevenue;
    private Double pendingRevenue;
    private Double cancelledRevenue;
    private Double averageOrderValue;
    private Double revenueGrowth;
    private Map<LocalDate, Double> revenueByDay;
    private Map<String, Double> revenueByWeek;
    private Map<String, Double> revenueByMonth;
    private Map<String, Double> revenueByYear;
    private Map<LocalDate, Double> topRevenueDays;
    private String currency;
    private LocalDate generatedAt;
}