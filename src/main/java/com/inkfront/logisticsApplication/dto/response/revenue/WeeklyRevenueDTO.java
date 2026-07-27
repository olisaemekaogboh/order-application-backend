// dto/response/revenue/WeeklyRevenueDTO.java
package com.inkfront.logisticsApplication.dto.response.revenue;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class WeeklyRevenueDTO {

    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private Integer weekNumber;
    private Integer year;
    private Double totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
    private String currency;
    private List<DailyRevenueDTO> dailyBreakdown;
    private Map<Integer, Double> breakdownByDay;
}