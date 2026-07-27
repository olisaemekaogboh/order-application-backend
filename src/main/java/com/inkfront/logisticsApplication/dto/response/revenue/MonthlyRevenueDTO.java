// dto/response/revenue/MonthlyRevenueDTO.java
package com.inkfront.logisticsApplication.dto.response.revenue;

import lombok.Data;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Data
public class MonthlyRevenueDTO {

    private YearMonth month;
    private Integer year;
    private Integer monthValue;
    private String monthName;
    private Double totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
    private String currency;
    private List<WeeklyRevenueDTO> weeklyBreakdown;
    private Map<Integer, Double> breakdownByDayOfMonth;
}