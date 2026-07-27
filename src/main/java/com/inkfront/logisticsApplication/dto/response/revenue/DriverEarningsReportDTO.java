package com.inkfront.logisticsApplication.dto.response.revenue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverEarningsReportDTO {

    private String driverId;
    private String driverName;
    private Double totalEarnings;
    private Double totalCommission;
    private Double netEarnings;
    private Long totalDeliveries;
    private Double averageEarningPerDelivery;
    private Map<String, Double> earningsByDay;
    private String currency;
}