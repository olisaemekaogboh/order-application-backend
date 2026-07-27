package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.dashboard.*;
import com.inkfront.logisticsApplication.dto.response.admin.DashboardStatsDTO;
import com.inkfront.logisticsApplication.dto.response.dashboard.*;

import java.util.Map;

public interface DashboardService {

    // Existing methods (unchanged)
    DashboardStatsDTO getAdminDashboardStats();
    DashboardStatsDTO getSuperAdminDashboardStats();
    DashboardStatsDTO getClientDashboardStats(String userId);
    Map<String, Object> getRevenueChartData(String period);
    Map<String, Object> getOrdersChartData(String period);
    Map<String, Object> getDriversChartData();
    Map<String, Object> getPaymentChartData(String period);

    // New methods using request DTOs
    DashboardStatsDTO getDashboardSummary(DashboardFilterRequestDTO request);
    RevenueAnalyticsDTO getRevenueAnalytics(RevenueAnalyticsRequestDTO request);
    DriverAnalyticsDTO getDriverAnalytics(DriverAnalyticsRequestDTO request);
    OrderAnalyticsDTO getOrderAnalytics(OrderAnalyticsRequestDTO request);
}