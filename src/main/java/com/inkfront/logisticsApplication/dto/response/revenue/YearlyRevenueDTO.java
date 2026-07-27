// dto/response/revenue/YearlyRevenueDTO.java
package com.inkfront.logisticsApplication.dto.response.revenue;

import lombok.Data;

import java.time.Year;
import java.util.List;
import java.util.Map;

@Data
public class YearlyRevenueDTO {

    private Year year;
    private Integer yearValue;
    private Double totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
    private String currency;
    private List<MonthlyRevenueDTO> monthlyBreakdown;
    private Map<Integer, Double> breakdownByMonth;
    private Double yearOverYearGrowth;
    private Double quarterOverQuarterGrowth;
}