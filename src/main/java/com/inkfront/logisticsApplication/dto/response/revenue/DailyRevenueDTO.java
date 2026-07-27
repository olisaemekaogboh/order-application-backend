// dto/response/revenue/DailyRevenueDTO.java
package com.inkfront.logisticsApplication.dto.response.revenue;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class DailyRevenueDTO {

    private LocalDate date;
    private Double totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
    private String currency;
    private Integer dayOfWeek;
    private String dayName;
    private Map<String, Double> breakdownByVehicleType;
    private Map<String, Double> breakdownByPaymentMethod;
}