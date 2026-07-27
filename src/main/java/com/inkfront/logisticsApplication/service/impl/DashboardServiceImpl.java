// service/impl/DashboardServiceImpl.java
package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.domain.enums.UserRole;
import com.inkfront.logisticsApplication.dto.response.admin.DashboardStatsDTO;
import com.inkfront.logisticsApplication.repository.*;
import com.inkfront.logisticsApplication.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Override
    public DashboardStatsDTO getAdminDashboardStats() {
        log.info("Fetching admin dashboard stats");

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime yearStart = LocalDate.now().minusDays(365).atStartOfDay();

        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Order Stats
        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus(OrderStatus.PENDING));
        stats.setAssignedOrders(orderRepository.countByStatus(OrderStatus.ASSIGNED));
        stats.setInTransitOrders(orderRepository.countByStatus(OrderStatus.IN_TRANSIT));
        stats.setDeliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED));
        stats.setCancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED));
        stats.setTodaysOrders(orderRepository.countOrdersBetweenDates(todayStart, todayEnd));

        // Revenue Stats
        stats.setTotalRevenue(calculateTotalRevenue());
        stats.setTodaysRevenue(calculateRevenueBetweenDates(todayStart, todayEnd));
        stats.setWeeklyRevenue(calculateRevenueBetweenDates(weekStart, todayEnd));
        stats.setMonthlyRevenue(calculateRevenueBetweenDates(monthStart, todayEnd));
        stats.setYearlyRevenue(calculateRevenueBetweenDates(yearStart, todayEnd));
        stats.setAverageOrderValue(calculateAverageOrderValue());

        // User Stats
        stats.setTotalUsers(userRepository.count());
        stats.setTotalClients(userRepository.countByRole(UserRole.CLIENT));
        stats.setTotalAdmins(userRepository.countByRole(UserRole.ADMIN) +
                userRepository.countByRole(UserRole.SUPER_ADMIN));
        stats.setTotalSuperAdmins(userRepository.countByRole(UserRole.SUPER_ADMIN));
        stats.setNewUsersToday(userRepository.countNewUsersSince(todayStart));
        stats.setActiveUsersToday(userRepository.countActiveUsersSince(todayStart));
        stats.setTotalAddresses(countTotalAddresses());

        // Driver Stats
        stats.setTotalDrivers(driverRepository.count());
        stats.setAvailableDrivers(driverRepository.countAvailableDrivers());
        stats.setBusyDrivers(driverRepository.count() - driverRepository.countAvailableDrivers());
        stats.setAverageDriverRating(driverRepository.calculateAverageRating());
        stats.setTotalDeliveriesToday(countDeliveriesToday());

        // Payment Stats
        stats.setTotalPayments(paymentTransactionRepository.count());
        stats.setTotalPendingPayments(calculateTotalPendingPayments());
        stats.setPaidOrders(orderRepository.countByPaymentStatus(PaymentStatus.PAID));
        stats.setPendingPayments(orderRepository.countByPaymentStatus(PaymentStatus.PENDING));

        // Chart Data
        stats.setRevenueChartData(getRevenueChartData("WEEK"));
        stats.setOrdersChartData(getOrdersChartData("WEEK"));
        stats.setDriversChartData(getDriversChartData());
        stats.setPaymentChartData(getPaymentChartData("WEEK"));

        // Formatted Stats
        stats.setFormattedTotalRevenue(formatCurrency(stats.getTotalRevenue()));
        stats.setFormattedTodaysRevenue(formatCurrency(stats.getTodaysRevenue()));
        stats.setFormattedAverageOrderValue(formatCurrency(stats.getAverageOrderValue()));

        // Growth Metrics
        Map<String, Double> growth = new HashMap<>();
        growth.put("revenueGrowth", calculateRevenueGrowth());
        growth.put("orderGrowth", calculateOrderGrowth());
        growth.put("userGrowth", calculateUserGrowth());
        stats.setGrowthPercentage(growth);

        return stats;
    }

    @Override
    public DashboardStatsDTO getSuperAdminDashboardStats() {
        log.info("Fetching super admin dashboard stats");
        return getAdminDashboardStats();
    }

    @Override
    public DashboardStatsDTO getClientDashboardStats(String userId) {
        log.info("Fetching client dashboard stats for user: {}", userId);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);

        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Order Stats - using existing repository methods
        stats.setTotalOrders(orderRepository.countByUserId(userId));
        stats.setPendingOrders(countByUserIdAndStatus(userId, OrderStatus.PENDING));
        stats.setInTransitOrders(countByUserIdAndStatus(userId, OrderStatus.IN_TRANSIT));
        stats.setDeliveredOrders(countByUserIdAndStatus(userId, OrderStatus.DELIVERED));
        stats.setCancelledOrders(countByUserIdAndStatus(userId, OrderStatus.CANCELLED));
        stats.setTodaysOrders(orderRepository.countByUserIdAndDateRange(userId, todayStart, todayEnd));

        // Revenue
        stats.setTotalRevenue(calculateUserTotalRevenue(userId));
        stats.setTodaysRevenue(calculateUserRevenueBetweenDates(userId, todayStart, todayEnd));
        stats.setAverageOrderValue(calculateUserAverageOrderValue(userId));

        // Addresses - placeholder
        stats.setTotalAddresses(0L);

        // Formatted Stats
        stats.setFormattedTotalRevenue(formatCurrency(stats.getTotalRevenue()));
        stats.setFormattedTodaysRevenue(formatCurrency(stats.getTodaysRevenue()));
        stats.setFormattedAverageOrderValue(formatCurrency(stats.getAverageOrderValue()));

        return stats;
    }

    @Override
    public Map<String, Object> getRevenueChartData(String period) {
        log.info("Fetching revenue chart data for period: {}", period);

        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;

        switch (period.toUpperCase()) {
            case "DAY":
                startDate = endDate.minusDays(1);
                break;
            case "WEEK":
                startDate = endDate.minusDays(7);
                break;
            case "MONTH":
                startDate = endDate.minusDays(30);
                break;
            case "YEAR":
                startDate = endDate.minusDays(365);
                break;
            default:
                startDate = endDate.minusDays(7);
        }

        LocalDateTime current = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while (current.isBefore(endDate) || current.equals(endDate)) {
            LocalDateTime dayStart = current.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = current.withHour(23).withMinute(59).withSecond(59);

            Double revenue = calculateRevenueBetweenDates(dayStart, dayEnd);
            labels.add(current.format(formatter));
            data.add(revenue != null ? revenue : 0.0);

            current = current.plusDays(1);
        }

        chartData.put("labels", labels);
        chartData.put("data", data);
        chartData.put("label", "Revenue (" + period + ")");
        chartData.put("currency", "NGN");

        return chartData;
    }

    @Override
    public Map<String, Object> getOrdersChartData(String period) {
        log.info("Fetching orders chart data for period: {}", period);

        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;

        switch (period.toUpperCase()) {
            case "DAY":
                startDate = endDate.minusDays(1);
                break;
            case "WEEK":
                startDate = endDate.minusDays(7);
                break;
            case "MONTH":
                startDate = endDate.minusDays(30);
                break;
            case "YEAR":
                startDate = endDate.minusDays(365);
                break;
            default:
                startDate = endDate.minusDays(7);
        }

        LocalDateTime current = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while (current.isBefore(endDate) || current.equals(endDate)) {
            LocalDateTime dayStart = current.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = current.withHour(23).withMinute(59).withSecond(59);

            Long count = orderRepository.countOrdersBetweenDates(dayStart, dayEnd);
            labels.add(current.format(formatter));
            data.add(count != null ? count : 0L);

            current = current.plusDays(1);
        }

        chartData.put("labels", labels);
        chartData.put("data", data);
        chartData.put("label", "Orders (" + period + ")");

        return chartData;
    }

    @Override
    public Map<String, Object> getDriversChartData() {
        log.info("Fetching drivers chart data");

        Map<String, Object> chartData = new HashMap<>();

        List<String> labels = Arrays.asList("Available", "Busy", "Offline");
        List<Long> data = new ArrayList<>();

        long totalDrivers = driverRepository.count();
        long availableDrivers = driverRepository.countAvailableDrivers();
        long busyDrivers = driverRepository.countBusyDrivers();
        long offlineDrivers = totalDrivers - availableDrivers - busyDrivers;

        data.add(availableDrivers);
        data.add(busyDrivers);
        data.add(offlineDrivers);

        chartData.put("labels", labels);
        chartData.put("data", data);
        chartData.put("label", "Driver Status Distribution");

        return chartData;
    }

    @Override
    public Map<String, Object> getPaymentChartData(String period) {
        log.info("Fetching payment chart data for period: {}", period);

        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        // Payment status distribution
        labels.add("Paid");
        labels.add("Pending");
        labels.add("Failed");
        labels.add("Refunded");

        data.add(orderRepository.countByPaymentStatus(PaymentStatus.PAID));
        data.add(orderRepository.countByPaymentStatus(PaymentStatus.PENDING));
        data.add(orderRepository.countByPaymentStatus(PaymentStatus.FAILED));
        data.add(orderRepository.countByPaymentStatus(PaymentStatus.REFUNDED));

        chartData.put("labels", labels);
        chartData.put("data", data);
        chartData.put("label", "Payment Status Distribution");

        return chartData;
    }

    // Private helper methods

    private Double calculateTotalRevenue() {
        return orderRepository.sumTotalPriceByStatus(OrderStatus.DELIVERED);
    }

    private Double calculateRevenueBetweenDates(LocalDateTime start, LocalDateTime end) {
        return orderRepository.sumTotalPriceBetweenDates(start, end);
    }

    private Double calculateAverageOrderValue() {
        Long totalOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        Double totalRevenue = calculateTotalRevenue();
        if (totalOrders > 0 && totalOrders != null && totalRevenue != null) {
            return totalRevenue / totalOrders;
        }
        return 0.0;
    }

    private Double calculateTotalPendingPayments() {
        return paymentTransactionRepository.sumAmountByStatus(PaymentStatus.PENDING);
    }

    private Long countDeliveriesToday() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        return orderRepository.countDeliveredBetweenDates(todayStart, todayEnd);
    }

    private Long countTotalAddresses() {
        // This would need an address repository
        // For now, return 0 or implement if you have AddressRepository
        return 0L;
    }

    private Long countByUserIdAndStatus(String userId, OrderStatus status) {
        try {
            return orderRepository.countByUserIdAndStatus(userId, status);
        } catch (Exception e) {
            return 0L;
        }
    }

    private Double calculateUserTotalRevenue(String userId) {
        try {
            return orderRepository.sumTotalPriceByUserIdAndStatus(userId, OrderStatus.DELIVERED);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Double calculateUserRevenueBetweenDates(String userId, LocalDateTime start, LocalDateTime end) {
        try {
            return orderRepository.sumTotalPriceByUserIdAndDateRange(userId, start, end);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Double calculateUserAverageOrderValue(String userId) {
        Long totalOrders = countByUserIdAndStatus(userId, OrderStatus.DELIVERED);
        Double totalRevenue = calculateUserTotalRevenue(userId);
        if (totalOrders > 0 && totalOrders != null && totalRevenue != null) {
            return totalRevenue / totalOrders;
        }
        return 0.0;
    }

    private Double calculateRevenueGrowth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thisMonthStart = now.minusDays(30);
        LocalDateTime lastMonthStart = now.minusDays(60);
        LocalDateTime lastMonthEnd = now.minusDays(30);

        Double thisMonthRevenue = calculateRevenueBetweenDates(thisMonthStart, now);
        Double lastMonthRevenue = calculateRevenueBetweenDates(lastMonthStart, lastMonthEnd);

        if (lastMonthRevenue != null && lastMonthRevenue > 0 && thisMonthRevenue != null) {
            return ((thisMonthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100;
        }
        return 0.0;
    }

    private Double calculateOrderGrowth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thisMonthStart = now.minusDays(30);
        LocalDateTime lastMonthStart = now.minusDays(60);
        LocalDateTime lastMonthEnd = now.minusDays(30);

        Long thisMonthOrders = orderRepository.countOrdersBetweenDates(thisMonthStart, now);
        Long lastMonthOrders = orderRepository.countOrdersBetweenDates(lastMonthStart, lastMonthEnd);

        if (lastMonthOrders != null && lastMonthOrders > 0 && thisMonthOrders != null) {
            return ((double) (thisMonthOrders - lastMonthOrders) / lastMonthOrders) * 100;
        }
        return 0.0;
    }

    private Double calculateUserGrowth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thisMonthStart = now.minusDays(30);

        Long thisMonthUsers = userRepository.countNewUsersSince(thisMonthStart);
        Long lastMonthUsers = userRepository.countNewUsersSince(thisMonthStart.minusDays(30));

        if (lastMonthUsers > 0 && lastMonthUsers != null && thisMonthUsers != null) {
            return ((double) (thisMonthUsers - lastMonthUsers) / lastMonthUsers) * 100;
        }
        return 0.0;
    }

    private String formatCurrency(Double amount) {
        if (amount == null) {
            return "₦0.00";
        }
        return "₦" + String.format("%,.2f", amount);
    }
}