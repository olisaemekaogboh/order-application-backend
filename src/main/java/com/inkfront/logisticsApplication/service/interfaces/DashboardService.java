package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.response.admin.DashboardStatsDTO;

import java.util.Map;

public interface DashboardService {

    DashboardStatsDTO getAdminDashboardStats();

    DashboardStatsDTO getSuperAdminDashboardStats();

    DashboardStatsDTO getClientDashboardStats(String userId);

    Map<String, Object> getRevenueChartData(String period);

    Map<String, Object> getOrdersChartData(String period);

    Map<String, Object> getDriversChartData();

    Map<String, Object> getPaymentChartData(String period);
}