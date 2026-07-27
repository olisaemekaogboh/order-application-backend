package com.inkfront.logisticsApplication.dto.response.dashboard;

import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAnalyticsDTO {

    private Long totalOrders;
    private Map<OrderStatus, Long> ordersByStatus;
    private Long deliveredOrders;
    private Long cancelledOrders;
    private Long pendingOrders;
    private Double averageOrderValue;
    private String formattedAverageOrderValue;
    private Double orderGrowthPercentage;
}