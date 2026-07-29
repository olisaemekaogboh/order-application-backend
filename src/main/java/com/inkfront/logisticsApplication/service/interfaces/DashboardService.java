package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.dashboard.*;
import com.inkfront.logisticsApplication.dto.response.admin.DashboardStatsDTO;
import com.inkfront.logisticsApplication.dto.response.dashboard.*;
import com.inkfront.logisticsApplication.dto.response.review.ReviewAnalyticsDTO;

import java.util.Map;

public interface DashboardService {
    DashboardStatsDTO getAdminDashboardStats();
    DashboardStatsDTO getSuperAdminDashboardStats();
    DashboardStatsDTO getClientDashboardStats(String userId);
    Map<String, Object> getRevenueChartData(String period);
    Map<String, Object> getOrdersChartData(String period);
    Map<String, Object> getDriversChartData();
    Map<String, Object> getPaymentChartData(String period);


    DashboardStatsDTO getDashboardSummary(DashboardFilterRequestDTO request);
    RevenueAnalyticsDTO getRevenueAnalytics(RevenueAnalyticsRequestDTO request);
    DriverAnalyticsDTO getDriverAnalytics(DriverAnalyticsRequestDTO request);
    OrderAnalyticsDTO getOrderAnalytics(OrderAnalyticsRequestDTO request);


    ReviewAnalyticsDTO getReviewAnalytics();

}