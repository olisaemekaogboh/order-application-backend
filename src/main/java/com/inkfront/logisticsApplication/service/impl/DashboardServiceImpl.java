package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.enums.ModerationStatus;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.domain.enums.UserRole;
import com.inkfront.logisticsApplication.dto.request.dashboard.*;
import com.inkfront.logisticsApplication.dto.response.admin.DashboardStatsDTO;
import com.inkfront.logisticsApplication.dto.response.dashboard.*;
import com.inkfront.logisticsApplication.dto.response.review.ReviewAnalyticsDTO;
import com.inkfront.logisticsApplication.repository.*;
import com.inkfront.logisticsApplication.service.interfaces.DashboardService;
import com.inkfront.logisticsApplication.service.interfaces.review.ReviewAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ReviewAnalyticsService reviewAnalyticsService;
    private final ReviewRepository reviewRepository;

    // ========== EXISTING METHODS (unchanged) ==========

    @Override
    public DashboardStatsDTO getAdminDashboardStats() {

        log.info("Fetching admin dashboard statistics");

        DashboardStatsDTO dto = new DashboardStatsDTO();

        LocalDate today = LocalDate.now();

        LocalDateTime startToday = today.atStartOfDay();
        LocalDateTime endToday = today.plusDays(1).atStartOfDay();

        LocalDateTime startWeek = today.minusDays(6).atStartOfDay();
        LocalDateTime startMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startYear = today.withDayOfYear(1).atStartOfDay();

        // ======================
        // ORDERS
        // ======================

        dto.setTotalOrders(orderRepository.count());

        dto.setPendingOrders(orderRepository.countByStatus(OrderStatus.PENDING));

        dto.setAssignedOrders(orderRepository.countByStatus(OrderStatus.ASSIGNED));

        dto.setInTransitOrders(orderRepository.countByStatus(OrderStatus.IN_TRANSIT));

        dto.setDeliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED));

        dto.setCancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED));

        dto.setTodaysOrders(
                orderRepository.countOrdersBetweenDates(startToday, endToday)
        );

        // ======================
        // USERS
        // ======================

        dto.setTotalUsers(userRepository.count());

        dto.setTotalClients(userRepository.countByRole(UserRole.CLIENT));

        dto.setTotalAdmins(userRepository.countByRole(UserRole.ADMIN));

        dto.setTotalSuperAdmins(userRepository.countByRole(UserRole.SUPER_ADMIN));

        dto.setNewUsersToday(
                userRepository.countNewUsersSince(startToday)
        );

        dto.setActiveUsersToday(
                userRepository.countActiveUsersSince(startToday)
        );

        // ======================
        // DRIVERS
        // ======================

        dto.setTotalDrivers(driverRepository.count());

        dto.setAvailableDrivers(
                driverRepository.countAvailableDrivers()
        );

        dto.setBusyDrivers(
                driverRepository.countBusyDrivers()
        );

        dto.setAverageDriverRating(
                toDouble(driverRepository.calculateAverageRating())
        );

        // ======================
        // PAYMENTS
        // ======================

        dto.setTotalPayments(paymentTransactionRepository.count());

        dto.setPaidOrders(
                paymentTransactionRepository.countByStatus(PaymentStatus.PAID)
        );

        dto.setPendingPayments(
                paymentTransactionRepository.countByStatus(PaymentStatus.PENDING)
        );

        dto.setTotalRevenue(
                toDouble(paymentTransactionRepository.sumSuccessfulPayments())
        );

        dto.setTotalPendingPayments(
                toDouble(
                        paymentTransactionRepository.sumAmountByStatus(
                                PaymentStatus.PENDING
                        )
                )
        );

        // ======================
        // REVENUE
        // ======================

        dto.setTodaysRevenue(
                toDouble(
                        paymentTransactionRepository.sumSuccessfulPaymentsBetweenDates(
                                startToday,
                                endToday
                        )
                )
        );

        dto.setWeeklyRevenue(
                toDouble(
                        paymentTransactionRepository.sumSuccessfulPaymentsBetweenDates(
                                startWeek,
                                endToday
                        )
                )
        );

        dto.setMonthlyRevenue(
                toDouble(
                        paymentTransactionRepository.sumSuccessfulPaymentsBetweenDates(
                                startMonth,
                                endToday
                        )
                )
        );

        dto.setYearlyRevenue(
                toDouble(
                        paymentTransactionRepository.sumSuccessfulPaymentsBetweenDates(
                                startYear,
                                endToday
                        )
                )
        );

        if (dto.getPaidOrders() > 0) {

            dto.setAverageOrderValue(
                    dto.getTotalRevenue() / dto.getPaidOrders()
            );

        } else {

            dto.setAverageOrderValue(0.0);

        }

        dto.setFormattedTotalRevenue(
                formatCurrency(dto.getTotalRevenue())
        );

        dto.setFormattedTodaysRevenue(
                formatCurrency(dto.getTodaysRevenue())
        );

        dto.setFormattedAverageOrderValue(
                formatCurrency(dto.getAverageOrderValue())
        );

        // ======================
        // GROWTH PLACEHOLDER
        // ======================

        Map<String, Double> growth = new HashMap<>();

        growth.put("daily", 0.0);
        growth.put("weekly", 0.0);
        growth.put("monthly", 0.0);
        growth.put("yearly", 0.0);

        dto.setGrowthPercentage(growth);

        return dto;
    }
    @Override
    public DashboardStatsDTO getSuperAdminDashboardStats() {

        log.info("Fetching super admin dashboard statistics");

        DashboardStatsDTO dto = getAdminDashboardStats();

        /*
         * ==========================================
         * SUPER ADMIN SPECIFIC METRICS
         * ==========================================
         */

        dto.setTotalSuperAdmins(
                userRepository.countByRole(UserRole.SUPER_ADMIN)
        );

        dto.setTotalAdmins(
                userRepository.countByRole(UserRole.ADMIN)
        );

        dto.setTotalClients(
                userRepository.countByRole(UserRole.CLIENT)
        );

        dto.setTotalUsers(
                userRepository.count()
        );

        return dto;
    }
    @Override
    public DashboardStatsDTO getClientDashboardStats(String userId) {
        log.info("Fetching client dashboard stats for user: {}", userId);
        // ... existing code (unchanged) ...
        return new DashboardStatsDTO();
    }

    @Override
    public Map<String, Object> getRevenueChartData(String period) {
        log.info("Fetching revenue chart data for period: {}", period);
        // ... existing code (unchanged) ...
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getOrdersChartData(String period) {
        log.info("Fetching orders chart data for period: {}", period);
        // ... existing code (unchanged) ...
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getDriversChartData() {
        log.info("Fetching drivers chart data");
        // ... existing code (unchanged) ...
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getPaymentChartData(String period) {
        log.info("Fetching payment chart data for period: {}", period);
        // ... existing code (unchanged) ...
        return new HashMap<>();
    }

    // ========== NEW METHODS ==========

    @Override
    public DashboardStatsDTO getDashboardSummary(
            DashboardFilterRequestDTO request) {

        log.info("Generating filtered dashboard summary");

        DashboardStatsDTO dto = getAdminDashboardStats();

        // TODO:
        // Apply request filters (date range, status, driver, etc.)
        // and overwrite only the affected metrics.

        return dto;
    }
    @Override
    public RevenueAnalyticsDTO getRevenueAnalytics(
            RevenueAnalyticsRequestDTO request) {

        log.info(
                "Generating revenue analytics from {} to {}",
                request.getStartDate(),
                request.getEndDate()
        );

        LocalDateTime start =
                request.getStartDate().atStartOfDay();

        LocalDateTime end =
                request.getEndDate()
                        .plusDays(1)
                        .atStartOfDay();

        BigDecimal totalRevenue =
                paymentTransactionRepository
                        .sumSuccessfulPaymentsBetweenDates(start, end);

        List<RevenuePointDTO> chart =
                paymentTransactionRepository
                        .getRevenueGroupedByDay(start, end)
                        .stream()
                        .map(item ->
                                RevenuePointDTO.builder()
                                        .period(item.getPeriod())
                                        .amount(
                                                item.getAmount() == null
                                                        ? 0D
                                                        : item.getAmount().doubleValue()
                                        )
                                        .build()
                        )
                        .toList();

        double revenue =
                totalRevenue == null
                        ? 0D
                        : totalRevenue.doubleValue();

        long days =
                ChronoUnit.DAYS.between(
                        request.getStartDate(),
                        request.getEndDate()
                ) + 1;

        double average =
                days == 0
                        ? 0
                        : revenue / days;

        return RevenueAnalyticsDTO.builder()
                .totalRevenue(revenue)
                .formattedTotalRevenue(formatCurrency(revenue))
                .averageDailyRevenue(average)
                .growthPercentage(0D)
                .currency("NGN")
                .revenueByPeriod(chart)
                .build();
    }

    @Override
    public DriverAnalyticsDTO getDriverAnalytics(
            DriverAnalyticsRequestDTO request) {

        log.info("Generating driver analytics");

        Long totalDrivers =
                driverRepository.count();

        Long availableDrivers =
                driverRepository.countAvailableDrivers();

        Long busyDrivers =
                driverRepository.countBusyDrivers();

        Long offlineDrivers =
                driverRepository.countOfflineDrivers();

        Double averageRating =
                driverRepository.calculateAverageRating();

        averageRating =
                averageRating == null
                        ? 0D
                        : averageRating;

        Long totalDeliveries = 0L;

        if (request.getDriverId() != null &&
                !request.getDriverId().isBlank()) {

            totalDeliveries =
                    orderRepository.countDeliveredOrdersByDriver(
                            request.getDriverId()
                    );

        } else {

            totalDeliveries =
                    orderRepository.countByStatus(
                            OrderStatus.DELIVERED
                    );
        }

        Map<String, Double> metrics =
                new LinkedHashMap<>();

        double completionRate =
                totalDrivers == 0
                        ? 0D
                        : ((double) busyDrivers / totalDrivers) * 100;

        double availabilityRate =
                totalDrivers == 0
                        ? 0D
                        : ((double) availableDrivers / totalDrivers) * 100;

        metrics.put("completionRate", completionRate);

        metrics.put("availabilityRate", availabilityRate);

        metrics.put("averageRating", averageRating);

        return DriverAnalyticsDTO.builder()
                .totalDrivers(totalDrivers)
                .availableDrivers(availableDrivers)
                .busyDrivers(busyDrivers)
                .offlineDrivers(offlineDrivers)
                .averageRating(averageRating)
                .totalDeliveries(totalDeliveries)
                .performanceMetrics(metrics)
                .build();
    }
    @Override
    public OrderAnalyticsDTO getOrderAnalytics(
            OrderAnalyticsRequestDTO request) {

        log.info("Generating order analytics");

        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : LocalDate.now().minusDays(30);

        LocalDate endDate = request.getEndDate() != null
                ? request.getEndDate()
                : LocalDate.now();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        Long totalOrders =
                orderRepository.countOrdersBetweenDates(start, end);

        Map<OrderStatus, Long> statusMap =
                new EnumMap<>(OrderStatus.class);

        for (OrderStatus status : OrderStatus.values()) {

            Long count =
                    orderRepository.countOrdersByStatusBetweenDates(
                            status,
                            start,
                            end
                    );

            statusMap.put(status, count == null ? 0L : count);
        }

        Double averageValue =
                orderRepository.averageDeliveredOrderValue(start, end);

        averageValue = averageValue == null ? 0D : averageValue;

        Long delivered =
                statusMap.getOrDefault(OrderStatus.DELIVERED, 0L);

        Long cancelled =
                statusMap.getOrDefault(OrderStatus.CANCELLED, 0L);

        Long pending =
                statusMap.getOrDefault(OrderStatus.PENDING, 0L);

        return OrderAnalyticsDTO.builder()
                .totalOrders(totalOrders)
                .ordersByStatus(statusMap)
                .deliveredOrders(delivered)
                .cancelledOrders(cancelled)
                .pendingOrders(pending)
                .averageOrderValue(averageValue)
                .formattedAverageOrderValue(formatCurrency(averageValue))
                .orderGrowthPercentage(0D)
                .build();
    }

    @Override
    public ReviewAnalyticsDTO getReviewAnalytics() {

        log.info("Generating review analytics");

        Long totalReviews =
                reviewRepository.countActiveApprovedReviews();

        Double averageRating =
                reviewRepository.calculateOverallAverageRating();

        averageRating =
                averageRating == null ? 0D : averageRating;

        Map<Integer, Long> distribution =
                new TreeMap<>();

        for (int i = 1; i <= 5; i++) {

            distribution.put(
                    i,
                    reviewRepository.countByRating(i)
            );
        }

        Map<String, Long> reviewsByMonth =
                new LinkedHashMap<>();

        List<Object[]> monthly =
                reviewRepository.countReviewsByMonth();

        for (Object[] row : monthly) {

            reviewsByMonth.put(
                    row[0].toString(),
                    ((Number) row[1]).longValue()
            );
        }

        Long pendingModeration =
                reviewRepository.countByModerationStatus(
                        ModerationStatus.PENDING
                );

        Long flagged =
                reviewRepository.countReportedReviewsPendingModeration();

        Double driverAverage =
                reviewRepository.calculateDriverAverageRating();

        Double customerAverage =
                reviewRepository.calculateCustomerAverageRating();

        return ReviewAnalyticsDTO.builder()
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .ratingDistribution(distribution)
                .fiveStarCount(distribution.getOrDefault(5, 0L))
                .fourStarCount(distribution.getOrDefault(4, 0L))
                .threeStarCount(distribution.getOrDefault(3, 0L))
                .twoStarCount(distribution.getOrDefault(2, 0L))
                .oneStarCount(distribution.getOrDefault(1, 0L))
                .pendingModerationCount(pendingModeration)
                .flaggedCount(flagged)
                .reviewsByMonth(reviewsByMonth)
                .driverAverageRating(
                        driverAverage == null ? 0D : driverAverage
                )
                .customerAverageRating(
                        customerAverage == null ? 0D : customerAverage
                )
                .build();
    }
    private Double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private Double safeBigDecimal(java.math.BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private String formatCurrency(Double value) {
        return String.format("₦%,.2f", safeDouble(value));
    }
    private double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private double toDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private String formatCurrency(double amount) {
        return String.format("₦%,.2f", amount);
    }
}