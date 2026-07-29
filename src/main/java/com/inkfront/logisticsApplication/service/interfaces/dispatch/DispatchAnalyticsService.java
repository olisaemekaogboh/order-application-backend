package com.inkfront.logisticsApplication.service.interfaces.dispatch;

import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchAnalyticsDTO;

import java.time.LocalDate;
import java.util.Map;

public interface DispatchAnalyticsService {

    DispatchAnalyticsDTO getAnalytics();

    DispatchAnalyticsDTO getAnalyticsForDateRange(LocalDate startDate, LocalDate endDate);

    double getAverageDispatchTime();

    double getDriverAcceptanceRate();

    double getDispatchSuccessRate();

    Map<String, Long> getDispatchesGroupedByStatus();

    Map<String, Long> getDispatchesGroupedByDate(LocalDate startDate, LocalDate endDate);
}