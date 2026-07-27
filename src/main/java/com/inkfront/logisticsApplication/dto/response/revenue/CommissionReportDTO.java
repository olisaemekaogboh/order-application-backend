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
public class CommissionReportDTO {

    private Double totalCommission;
    private Long totalOrders;
    private Double averageCommissionPerOrder;
    private Map<String, Double> commissionByDriver;
    private Map<String, Double> commissionByDay;
    private String currency;
}