// dto/response/revenue/RevenueReportDTO.java
package com.inkfront.logisticsApplication.dto.response.revenue;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class RevenueReportDTO {

    private String id;
    private String reportPeriod;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
    private Double totalCommission;
    private Double totalDriverPayout;
    private Double netRevenue;
    private String currency;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private String reportName;

    private List<DailyRevenueDTO> dailyBreakdown;
    private List<WeeklyRevenueDTO> weeklyBreakdown;
    private Map<String, Double> revenueByState;
    private Map<String, Double> revenueByVehicleType;
    private Map<String, Double> revenueByPaymentMethod;
    private Map<String, Double> revenueByStatus;

    private String formattedTotalRevenue;
    private String formattedAverageOrderValue;
    private String formattedTotalCommission;
    private String formattedNetRevenue;
}