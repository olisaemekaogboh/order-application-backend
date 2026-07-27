// dto/response/admin/DashboardStatsDTO.java
package com.inkfront.logisticsApplication.dto.response.admin;

import lombok.Data;

import java.util.Map;

@Data
public class DashboardStatsDTO {

    // Order Stats
    private Long totalOrders;
    private Long pendingOrders;
    private Long assignedOrders;
    private Long inTransitOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    private Long todaysOrders;

    // Revenue Stats
    private Double totalRevenue;
    private Double todaysRevenue;
    private Double weeklyRevenue;
    private Double monthlyRevenue;
    private Double yearlyRevenue;
    private Double averageOrderValue;

    // User Stats
    private Long totalUsers;
    private Long totalClients;
    private Long totalAdmins;
    private Long totalSuperAdmins;  // Add this field
    private Long newUsersToday;
    private Long activeUsersToday;
    private Long totalAddresses;  // Add this field

    // Driver Stats
    private Long totalDrivers;
    private Long availableDrivers;
    private Long busyDrivers;
    private Double averageDriverRating;
    private Long totalDeliveriesToday;

    // Payment Stats
    private Long totalPayments;
    private Double totalPendingPayments;
    private Long paidOrders;
    private Long pendingPayments;

    // Charts Data
    private Map<String, Object> revenueChartData;
    private Map<String, Object> ordersChartData;
    private Map<String, Object> driversChartData;
    private Map<String, Object> paymentChartData;

    // Formatted Stats
    private String formattedTotalRevenue;
    private String formattedTodaysRevenue;
    private String formattedAverageOrderValue;

    // Growth Metrics
    private Map<String, Double> growthPercentage;
}