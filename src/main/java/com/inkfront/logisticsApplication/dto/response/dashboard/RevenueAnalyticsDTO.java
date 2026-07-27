package com.inkfront.logisticsApplication.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueAnalyticsDTO {

    private Double totalRevenue;
    private String formattedTotalRevenue;
    private Double averageDailyRevenue;
    private Double growthPercentage;
    private List<Map<String, Object>> revenueByPeriod; // period -> amount
    private String currency;
}