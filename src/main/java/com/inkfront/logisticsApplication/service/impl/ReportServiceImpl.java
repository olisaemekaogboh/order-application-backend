package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.UserRole;
import com.inkfront.logisticsApplication.dto.request.report.*;
import com.inkfront.logisticsApplication.dto.response.report.*;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.repository.PaymentTransactionRepository;
import com.inkfront.logisticsApplication.service.interfaces.ReportService;
import com.inkfront.logisticsApplication.util.report.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PdfReportExporter pdfExporter;
    private final ExcelReportExporter excelExporter;
    private final CsvReportExporter csvExporter;
    private final ReportUtils reportUtils;

    // ==================== HELPER: Get default dates ====================

    private LocalDate getDefaultStartDate() {
        return LocalDate.now().minusDays(30);
    }

    private LocalDate getDefaultEndDate() {
        return LocalDate.now();
    }

    private LocalDate getStartDate(LocalDate startDate) {
        return startDate != null ? startDate : getDefaultStartDate();
    }

    private LocalDate getEndDate(LocalDate endDate) {
        return endDate != null ? endDate : getDefaultEndDate();
    }

    // ==================== REVENUE REPORT ====================

    @Override
    public RevenueReportDTO generateRevenueReport(RevenueReportRequestDTO request) {
        LocalDate startDate = getStartDate(request.getStartDate());
        LocalDate endDate = getEndDate(request.getEndDate());

        log.info("Generating revenue report from {} to {}", startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findOrdersBetweenDates(start, end);
        List<Order> completed = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());

        double currentRevenue = completed.stream().mapToDouble(Order::getTotalPrice).sum();

        long days = reportUtils.getDaysBetween(startDate, endDate);
        LocalDate prevStart = startDate.minusDays(days);
        LocalDate prevEnd = endDate.minusDays(days);
        List<Order> prevOrders = orderRepository.findOrdersBetweenDates(prevStart.atStartOfDay(), prevEnd.atTime(23, 59, 59));
        double previousRevenue = prevOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(Order::getTotalPrice)
                .sum();

        Map<LocalDate, Double> byDay = completed.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getOrderDate().toLocalDate(),
                        Collectors.summingDouble(Order::getTotalPrice)
                ));

        Map<String, Double> byWeek = completed.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-ww")),
                        Collectors.summingDouble(Order::getTotalPrice)
                ));

        Map<String, Double> byMonth = completed.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.summingDouble(Order::getTotalPrice)
                ));

        Map<String, Double> byYear = completed.stream()
                .collect(Collectors.groupingBy(
                        o -> String.valueOf(o.getOrderDate().getYear()),
                        Collectors.summingDouble(Order::getTotalPrice)
                ));

        Map<LocalDate, Double> topDays = byDay.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        double growth = reportUtils.calculateRevenueGrowth(currentRevenue, previousRevenue);

        ReportSummaryDTO summary = ReportSummaryDTO.builder()
                .title("Revenue Report")
                .startDate(startDate)
                .endDate(endDate)
                .generatedBy(getCurrentUsername())
                .generatedAt(LocalDate.now())
                .build();

        return RevenueReportDTO.builder()
                .summary(summary)
                .totalRevenue(currentRevenue)
                .completedRevenue(currentRevenue)
                .pendingRevenue(0.0)
                .cancelledRevenue(0.0)
                .averageOrderValue(completed.isEmpty() ? 0.0 : currentRevenue / completed.size())
                .revenueGrowth(growth)
                .revenueByDay(byDay)
                .revenueByWeek(byWeek)
                .revenueByMonth(byMonth)
                .revenueByYear(byYear)
                .topRevenueDays(topDays)
                .currency("NGN")
                .generatedAt(LocalDate.now())
                .build();
    }

    // ==================== ORDER REPORT ====================

    @Override
    public OrderReportDTO generateOrderReport(OrderReportRequestDTO request) {
        LocalDate startDate = getStartDate(request.getStartDate());
        LocalDate endDate = getEndDate(request.getEndDate());

        log.info("Generating order report from {} to {}", startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findOrdersBetweenDates(start, end);

        long total = orders.size();
        long completed = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long pending = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long inTransit = orders.stream().filter(o -> o.getStatus() == OrderStatus.IN_TRANSIT).count();
        long delivered = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();

        double avgDeliveryTime = completed > 0 ?
                orders.stream()
                        .filter(o -> o.getStatus() == OrderStatus.DELIVERED && o.getDeliveryDate() != null)
                        .mapToLong(o -> Duration.between(o.getOrderDate(), o.getDeliveryDate()).toMinutes())
                        .average().orElse(0.0) : 0.0;

        double avgDistance = orders.stream()
                .filter(o -> o.getDistanceKm() != null)
                .mapToDouble(Order::getDistanceKm)
                .average().orElse(0.0);

        String mostRequested = orders.stream()
                .filter(o -> o.getDriver() != null && o.getDriver().getVehicleType() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getDriver().getVehicleType().name(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        Map<LocalTime, Long> peakHours = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getOrderDate().toLocalTime().withMinute(0).withSecond(0),
                        Collectors.counting()
                ));

        Map<String, Long> byStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus().name(),
                        Collectors.counting()
                ));

        Map<String, Long> byLocation = orders.stream()
                .filter(o -> o.getDeliveryLocation() != null)
                .collect(Collectors.groupingBy(
                        o -> reportUtils.extractState(o.getDeliveryLocation()),
                        Collectors.counting()
                ));

        ReportSummaryDTO summary = ReportSummaryDTO.builder()
                .title("Order Report")
                .startDate(startDate)
                .endDate(endDate)
                .generatedBy(getCurrentUsername())
                .generatedAt(LocalDate.now())
                .build();

        return OrderReportDTO.builder()
                .summary(summary)
                .totalOrders(total)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .pendingOrders(pending)
                .inTransitOrders(inTransit)
                .deliveredOrders(delivered)
                .averageDeliveryTimeMinutes(avgDeliveryTime)
                .averageDistanceKm(avgDistance)
                .mostRequestedVehicle(mostRequested)
                .peakOrderHours(peakHours)
                .ordersByStatus(byStatus)
                .ordersByLocation(byLocation)
                .generatedAt(LocalDate.now())
                .build();
    }

    // ==================== DRIVER REPORT ====================

    @Override
    public DriverReportDTO generateDriverReport(DriverReportRequestDTO request) {
        LocalDate startDate = getStartDate(request.getStartDate());
        LocalDate endDate = getEndDate(request.getEndDate());

        log.info("Generating driver report from {} to {}", startDate, endDate);

        List<Driver> drivers = driverRepository.findAll();
        long total = drivers.size();
        long available = drivers.stream().filter(Driver::getAvailable).count();
        long busy = drivers.stream().filter(d -> !d.getAvailable() && d.getVerified()).count();
        long offline = total - available - busy;

        Map<String, Long> deliveriesByDriver = new HashMap<>();
        Map<String, Double> distanceByDriver = new HashMap<>();

        for (Driver driver : drivers) {
            List<Order> driverOrders = orderRepository.findByDriverIdAndStatusIn(
                    driver.getId(),
                    List.of(OrderStatus.DELIVERED)
            );
            long count = driverOrders.size();
            deliveriesByDriver.put(driver.getId(), count);

            double totalDist = driverOrders.stream()
                    .filter(o -> o.getDistanceKm() != null)
                    .mapToDouble(Order::getDistanceKm)
                    .sum();
            distanceByDriver.put(driver.getId(), totalDist);
        }

        long totalDeliveries = deliveriesByDriver.values().stream().mapToLong(Long::longValue).sum();
        double totalDistance = distanceByDriver.values().stream().mapToDouble(Double::doubleValue).sum();

        double avgRating = drivers.stream()
                .filter(d -> d.getRating() != null)
                .mapToDouble(Driver::getRating)
                .average().orElse(0.0);

        double revenueGenerated = 0.0;
        for (Driver driver : drivers) {
            List<Order> driverOrders = orderRepository.findByDriverIdAndStatusIn(
                    driver.getId(),
                    List.of(OrderStatus.DELIVERED)
            );
            double sum = driverOrders.stream().mapToDouble(Order::getTotalPrice).sum();
            revenueGenerated += sum;
        }

        ReportSummaryDTO summary = ReportSummaryDTO.builder()
                .title("Driver Report")
                .startDate(startDate)
                .endDate(endDate)
                .generatedBy(getCurrentUsername())
                .generatedAt(LocalDate.now())
                .build();

        return DriverReportDTO.builder()
                .summary(summary)
                .totalDrivers(total)
                .availableDrivers(available)
                .busyDrivers(busy)
                .offlineDrivers(offline)
                .completedDeliveries(totalDeliveries)
                .acceptanceRate(0.0)
                .cancellationRate(0.0)
                .averageRating(avgRating)
                .totalDistanceCoveredKm(totalDistance)
                .revenueGenerated(revenueGenerated)
                .driverPerformance(deliveriesByDriver)
                .generatedAt(LocalDate.now())
                .build();
    }

    // ==================== CUSTOMER REPORT ====================

    @Override
    public CustomerReportDTO generateCustomerReport(CustomerReportRequestDTO request) {
        LocalDate startDate = getStartDate(request.getStartDate());
        LocalDate endDate = getEndDate(request.getEndDate());

        log.info("Generating customer report from {} to {}", startDate, endDate);

        List<User> users = userRepository.findByRole(UserRole.CLIENT);

        long total = users.size();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        long newCustomers = users.stream()
                .filter(u -> u.getCreatedAt() != null && !u.getCreatedAt().isBefore(start) && !u.getCreatedAt().isAfter(end))
                .count();

        long returning = users.stream()
                .filter(u -> orderRepository.countByUserId(u.getId()) >= 2)
                .count();

        List<String> topCustomers = users.stream()
                .sorted((u1, u2) -> Long.compare(
                        orderRepository.countByUserId(u2.getId()),
                        orderRepository.countByUserId(u1.getId())
                ))
                .limit(5)
                .map(User::getFullName)
                .collect(Collectors.toList());

        List<String> mostActive = users.stream()
                .sorted((u1, u2) -> {
                    Double total1 = orderRepository.sumTotalPriceByUserIdAndStatus(u1.getId(), OrderStatus.DELIVERED);
                    Double total2 = orderRepository.sumTotalPriceByUserIdAndStatus(u2.getId(), OrderStatus.DELIVERED);
                    return Double.compare(total2 != null ? total2 : 0.0, total1 != null ? total1 : 0.0);
                })
                .limit(5)
                .map(User::getFullName)
                .collect(Collectors.toList());

        double avgSpend = users.isEmpty() ? 0.0 :
                users.stream()
                        .mapToDouble(u -> {
                            Double sum = orderRepository.sumTotalPriceByUserIdAndStatus(u.getId(), OrderStatus.DELIVERED);
                            return sum != null ? sum : 0.0;
                        })
                        .average().orElse(0.0);

        long completedOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        ReportSummaryDTO summary = ReportSummaryDTO.builder()
                .title("Customer Report")
                .startDate(startDate)
                .endDate(endDate)
                .generatedBy(getCurrentUsername())
                .generatedAt(LocalDate.now())
                .build();

        return CustomerReportDTO.builder()
                .summary(summary)
                .totalCustomers(total)
                .newCustomers(newCustomers)
                .returningCustomers(returning)
                .topCustomers(topCustomers)
                .mostActiveCustomers(mostActive)
                .averageSpend(avgSpend)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .customersByActivity(new HashMap<>())
                .generatedAt(LocalDate.now())
                .build();
    }

    // ==================== DELIVERY PERFORMANCE REPORT ====================

    @Override
    public DeliveryPerformanceReportDTO generateDeliveryReport(DeliveryReportRequestDTO request) {
        LocalDate startDate = getStartDate(request.getStartDate());
        LocalDate endDate = getEndDate(request.getEndDate());

        log.info("Generating delivery performance report from {} to {}", startDate, endDate);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findOrdersBetweenDates(start, end)
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());

        if (orders.isEmpty()) {
            return DeliveryPerformanceReportDTO.builder()
                    .summary(ReportSummaryDTO.builder()
                            .title("Delivery Performance Report")
                            .startDate(startDate)
                            .endDate(endDate)
                            .generatedBy(getCurrentUsername())
                            .generatedAt(LocalDate.now())
                            .build())
                    .averageDeliveryTime(Duration.ZERO)
                    .averagePickupTime(Duration.ZERO)
                    .averageDistanceKm(0.0)
                    .fastestDelivery(Duration.ZERO)
                    .longestDelivery(Duration.ZERO)
                    .delayedDeliveries(0L)
                    .onTimeDeliveryPercentage(0.0)
                    .deliveryTimesByDriver(new HashMap<>())
                    .generatedAt(LocalDate.now())
                    .build();
        }

        List<Duration> deliveryDurations = orders.stream()
                .filter(o -> o.getDeliveryDate() != null)
                .map(o -> Duration.between(o.getOrderDate(), o.getDeliveryDate()))
                .collect(Collectors.toList());

        Duration avgDelivery = deliveryDurations.stream()
                .reduce(Duration.ZERO, Duration::plus)
                .dividedBy(deliveryDurations.size());

        List<Duration> pickupDurations = orders.stream()
                .filter(o -> o.getPickupDate() != null)
                .map(o -> Duration.between(o.getOrderDate(), o.getPickupDate()))
                .collect(Collectors.toList());

        Duration avgPickup = pickupDurations.isEmpty() ? Duration.ZERO :
                pickupDurations.stream().reduce(Duration.ZERO, Duration::plus)
                        .dividedBy(pickupDurations.size());

        double avgDistance = orders.stream()
                .mapToDouble(Order::getDistanceKm)
                .average().orElse(0.0);

        Duration fastest = deliveryDurations.stream().min(Duration::compareTo).orElse(Duration.ZERO);
        Duration longest = deliveryDurations.stream().max(Duration::compareTo).orElse(Duration.ZERO);

        long delayed = orders.stream()
                .filter(o -> o.getEstimatedDeliveryDate() != null && o.getDeliveryDate() != null &&
                        o.getDeliveryDate().isAfter(o.getEstimatedDeliveryDate()))
                .count();

        double onTimePct = orders.isEmpty() ? 0.0 :
                ((double) (orders.size() - delayed) / orders.size()) * 100;

        Map<String, Duration> byDriver = orders.stream()
                .filter(o -> o.getDriver() != null && o.getDeliveryDate() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getDriver().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .map(d -> Duration.between(d.getOrderDate(), d.getDeliveryDate()))
                                        .reduce(Duration.ZERO, Duration::plus)
                                        .dividedBy(list.size())
                        )
                ));

        ReportSummaryDTO summary = ReportSummaryDTO.builder()
                .title("Delivery Performance Report")
                .startDate(startDate)
                .endDate(endDate)
                .generatedBy(getCurrentUsername())
                .generatedAt(LocalDate.now())
                .build();

        return DeliveryPerformanceReportDTO.builder()
                .summary(summary)
                .averageDeliveryTime(avgDelivery)
                .averagePickupTime(avgPickup)
                .averageDistanceKm(avgDistance)
                .fastestDelivery(fastest)
                .longestDelivery(longest)
                .delayedDeliveries(delayed)
                .onTimeDeliveryPercentage(onTimePct)
                .deliveryTimesByDriver(byDriver)
                .generatedAt(LocalDate.now())
                .build();
    }

    // ==================== DASHBOARD ANALYTICS ====================

    @Override
    public DashboardAnalyticsReportDTO generateDashboardAnalytics() {
        log.info("Generating dashboard analytics report");

        long totalOrders = orderRepository.count();
        Double totalRevenue = orderRepository.sumTotalPriceByStatus(OrderStatus.DELIVERED);
        long activeDrivers = driverRepository.countAvailableDrivers();
        long totalCustomers = userRepository.countByRole(UserRole.CLIENT);
        Double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("revenue", totalRevenue);
        chartData.put("orders", totalOrders);

        ReportSummaryDTO summary = ReportSummaryDTO.builder()
                .title("Dashboard Analytics")
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .generatedBy(getCurrentUsername())
                .generatedAt(LocalDate.now())
                .build();

        return DashboardAnalyticsReportDTO.builder()
                .summary(summary)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .activeDrivers(activeDrivers)
                .totalCustomers(totalCustomers)
                .averageOrderValue(avgOrderValue)
                .revenueGrowth(0.0)
                .chartData(chartData)
                .generatedAt(LocalDate.now())
                .build();
    }

    // ==================== EXPORT METHODS ====================
    // ✅ FIXED: Now using the exporter classes

    @Override
    public Resource exportRevenueReportToPdf(RevenueReportRequestDTO request) {
        log.info("Exporting revenue report to PDF using PdfReportExporter");
        RevenueReportDTO report = generateRevenueReport(request);
        return pdfExporter.exportRevenueReport(report);
    }

    @Override
    public Resource exportRevenueReportToExcel(RevenueReportRequestDTO request) {
        log.info("Exporting revenue report to Excel using ExcelReportExporter");
        RevenueReportDTO report = generateRevenueReport(request);
        return excelExporter.exportRevenueReport(report);
    }

    @Override
    public Resource exportRevenueReportToCsv(RevenueReportRequestDTO request) {
        log.info("Exporting revenue report to CSV using CsvReportExporter");
        RevenueReportDTO report = generateRevenueReport(request);
        return csvExporter.exportRevenueReport(report);
    }

    @Override
    public Resource exportOrderReportToPdf(OrderReportRequestDTO request) {
        log.info("Exporting order report to PDF using PdfReportExporter");
        OrderReportDTO report = generateOrderReport(request);
        return pdfExporter.exportOrderReport(report);
    }

    @Override
    public Resource exportOrderReportToExcel(OrderReportRequestDTO request) {
        log.info("Exporting order report to Excel using ExcelReportExporter");
        OrderReportDTO report = generateOrderReport(request);
        return excelExporter.exportOrderReport(report);
    }

    @Override
    public Resource exportOrderReportToCsv(OrderReportRequestDTO request) {
        log.info("Exporting order report to CSV using CsvReportExporter");
        OrderReportDTO report = generateOrderReport(request);
        return csvExporter.exportOrderReport(report);
    }

    @Override
    public Resource exportDriverReportToPdf(DriverReportRequestDTO request) {
        log.info("Exporting driver report to PDF using PdfReportExporter");
        DriverReportDTO report = generateDriverReport(request);
        return pdfExporter.exportDriverReport(report);
    }

    @Override
    public Resource exportDriverReportToExcel(DriverReportRequestDTO request) {
        log.info("Exporting driver report to Excel using ExcelReportExporter");
        DriverReportDTO report = generateDriverReport(request);
        return excelExporter.exportDriverReport(report);
    }

    @Override
    public Resource exportDriverReportToCsv(DriverReportRequestDTO request) {
        log.info("Exporting driver report to CSV using CsvReportExporter");
        DriverReportDTO report = generateDriverReport(request);
        return csvExporter.exportDriverReport(report);
    }

    @Override
    public Resource exportCustomerReportToPdf(CustomerReportRequestDTO request) {
        log.info("Exporting customer report to PDF using PdfReportExporter");
        CustomerReportDTO report = generateCustomerReport(request);
        return pdfExporter.exportCustomerReport(report);
    }

    @Override
    public Resource exportCustomerReportToExcel(CustomerReportRequestDTO request) {
        log.info("Exporting customer report to Excel using ExcelReportExporter");
        CustomerReportDTO report = generateCustomerReport(request);
        return excelExporter.exportCustomerReport(report);
    }

    @Override
    public Resource exportCustomerReportToCsv(CustomerReportRequestDTO request) {
        log.info("Exporting customer report to CSV using CsvReportExporter");
        CustomerReportDTO report = generateCustomerReport(request);
        return csvExporter.exportCustomerReport(report);
    }

    // ==================== HELPER ====================

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "System";
        }
    }
}