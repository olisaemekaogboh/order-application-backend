package com.inkfront.logisticsApplication.dto.response.dispatch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchAnalyticsDTO {

    private Long totalDispatches;
    private Long pending;
    private Long assigned;
    private Long accepted;
    private Long completed;
    private Long cancelled;
    private Long failed;
    private Double averageDispatchTimeMinutes;
    private Double driverAcceptanceRate;
    private Double successRate;
    private Map<String, Long> dispatchesByStatus;
}