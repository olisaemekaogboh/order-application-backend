package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.DriverEarning;
import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.RevenueReport;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.PaymentMethod;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.inkfront.logisticsApplication.domain.enums.ReportPeriod;
import com.inkfront.logisticsApplication.dto.request.admin.RevenueReportRequestDTO;
import com.inkfront.logisticsApplication.dto.request.revenue.*;
import com.inkfront.logisticsApplication.dto.response.revenue.*;
import com.inkfront.logisticsApplication.exception.BadRequestException;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.mapper.RevenueReportMapper;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.RevenueReportRepository;
import com.inkfront.logisticsApplication.repository.DriverEarningRepository;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.PaymentTransactionRepository;
import com.inkfront.logisticsApplication.service.interfaces.RevenueService;
import com.inkfront.logisticsApplication.util.DateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RevenueServiceImpl implements RevenueService {

    private final OrderRepository orderRepository;
    private final RevenueReportRepository revenueReportRepository;
    private final DriverEarningRepository driverEarningRepository;
    private final DriverRepository driverRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RevenueReportMapper revenueReportMapper;

    // ==================== EXISTING METHODS (unchanged) ====================

    @Override
    public RevenueReportDTO generateRevenueReport(RevenueReportRequestDTO request) {
        log.info("Generating revenue report for period: {}", request.getPeriod());

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            switch (request.getPeriod()) {
                case DAILY:
                    startDate = now;
                    endDate = now;
                    break;
                case WEEKLY:
                    startDate = now.minusDays(7);
                    endDate = now;
                    break;
                case MONTHLY:
                    startDate = now.withDayOfMonth(1);
                    endDate = now;
                    break;
                case YEARLY:
                    startDate = now.withDayOfYear(1);
                    endDate = now;
                    break;
            }
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findOrdersBetweenDatesAndStatus(
                startDateTime, endDateTime, OrderStatus.DELIVERED
        );

        double totalRevenue = orders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();

        long totalOrders = orders.size();
        double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;

        Double totalCommission = driverEarningRepository.sumAllCommissionsBetweenDates(startDateTime, endDateTime);
        Double totalDriverPayout = driverEarningRepository.sumAllEarningsBetweenDates(startDateTime, endDateTime);

        totalCommission = totalCommission != null ? totalCommission : 0.0;
        totalDriverPayout = totalDriverPayout != null ? totalDriverPayout : 0.0;

        RevenueReport report = new RevenueReport();
        report.setReportPeriod(request.getPeriod());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalRevenue(totalRevenue);
        report.setTotalOrders(totalOrders);
        report.setAverageOrderValue(averageOrderValue);
        report.setTotalCommission(totalCommission);
        report.setTotalDriverPayout(totalDriverPayout);
        report.setCurrency(request.getCurrency());
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportName("Revenue Report " + startDate + " to " + endDate);

        report = revenueReportRepository.save(report);

        RevenueReportDTO reportDTO = revenueReportMapper.toDTO(report);

        if (request.isIncludeBreakdown()) {
            reportDTO.setDailyBreakdown(getDailyRevenueRange(startDate, endDate));
            reportDTO.setRevenueByState(getRevenueByState(orders));
            reportDTO.setRevenueByVehicleType(getRevenueByVehicleType(orders));
            reportDTO.setRevenueByPaymentMethod(getRevenueByPaymentMethod(orders));
        }

        return reportDTO;
    }

    @Override
    public DailyRevenueDTO getDailyRevenue(LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findOrdersBetweenDatesAndStatus(startDate, endDate, OrderStatus.DELIVERED);

        DailyRevenueDTO dailyRevenue = new DailyRevenueDTO();
        dailyRevenue.setDate(date);
        dailyRevenue.setTotalRevenue(orders.stream().mapToDouble(Order::getTotalPrice).sum());
        dailyRevenue.setTotalOrders((long) orders.size());
        dailyRevenue.setAverageOrderValue(dailyRevenue.getTotalOrders() > 0 ?
                dailyRevenue.getTotalRevenue() / dailyRevenue.getTotalOrders() : 0.0);
        dailyRevenue.setCurrency("NGN");
        dailyRevenue.setDayOfWeek(date.getDayOfWeek().getValue());
        dailyRevenue.setDayName(date.getDayOfWeek().toString());
        dailyRevenue.setBreakdownByVehicleType(getRevenueByVehicleType(orders));
        dailyRevenue.setBreakdownByPaymentMethod(getRevenueByPaymentMethod(orders));

        return dailyRevenue;
    }

    @Override
    public List<DailyRevenueDTO> getDailyRevenueRange(LocalDate startDate, LocalDate endDate) {
        List<DailyRevenueDTO> dailyRevenues = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            dailyRevenues.add(getDailyRevenue(currentDate));
            currentDate = currentDate.plusDays(1);
        }

        return dailyRevenues;
    }

    @Override
    public WeeklyRevenueDTO getWeeklyRevenue(LocalDate date) {
        LocalDate weekStart = date.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = date.with(java.time.DayOfWeek.SUNDAY);

        List<DailyRevenueDTO> dailyBreakdown = getDailyRevenueRange(weekStart, weekEnd);

        WeeklyRevenueDTO weeklyRevenue = new WeeklyRevenueDTO();
        weeklyRevenue.setWeekStartDate(weekStart);
        weeklyRevenue.setWeekEndDate(weekEnd);
        weeklyRevenue.setWeekNumber(weekStart.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()));
        weeklyRevenue.setYear(weekStart.getYear());
        weeklyRevenue.setTotalRevenue(dailyBreakdown.stream().mapToDouble(DailyRevenueDTO::getTotalRevenue).sum());
        weeklyRevenue.setTotalOrders(dailyBreakdown.stream().mapToLong(DailyRevenueDTO::getTotalOrders).sum());
        weeklyRevenue.setAverageOrderValue(weeklyRevenue.getTotalOrders() > 0 ?
                weeklyRevenue.getTotalRevenue() / weeklyRevenue.getTotalOrders() : 0.0);
        weeklyRevenue.setCurrency("NGN");
        weeklyRevenue.setDailyBreakdown(dailyBreakdown);
        weeklyRevenue.setBreakdownByDay(dailyBreakdown.stream()
                .collect(Collectors.toMap(
                        d -> d.getDate().getDayOfWeek().getValue(),
                        DailyRevenueDTO::getTotalRevenue
                )));

        return weeklyRevenue;
    }

    @Override
    public List<WeeklyRevenueDTO> getWeeklyRevenueRange(LocalDate startDate, LocalDate endDate) {
        List<WeeklyRevenueDTO> weeklyRevenues = new ArrayList<>();
        LocalDate currentDate = startDate.with(java.time.DayOfWeek.MONDAY);

        while (!currentDate.isAfter(endDate)) {
            weeklyRevenues.add(getWeeklyRevenue(currentDate));
            currentDate = currentDate.plusWeeks(1);
        }

        return weeklyRevenues;
    }

    @Override
    public MonthlyRevenueDTO getMonthlyRevenue(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<WeeklyRevenueDTO> weeklyBreakdown = getWeeklyRevenueRange(startDate, endDate);

        MonthlyRevenueDTO monthlyRevenue = new MonthlyRevenueDTO();
        monthlyRevenue.setMonth(yearMonth);
        monthlyRevenue.setYear(year);
        monthlyRevenue.setMonthValue(month);
        monthlyRevenue.setMonthName(yearMonth.getMonth().toString());
        monthlyRevenue.setTotalRevenue(weeklyBreakdown.stream().mapToDouble(WeeklyRevenueDTO::getTotalRevenue).sum());
        monthlyRevenue.setTotalOrders(weeklyBreakdown.stream().mapToLong(WeeklyRevenueDTO::getTotalOrders).sum());
        monthlyRevenue.setAverageOrderValue(monthlyRevenue.getTotalOrders() > 0 ?
                monthlyRevenue.getTotalRevenue() / monthlyRevenue.getTotalOrders() : 0.0);
        monthlyRevenue.setCurrency("NGN");
        monthlyRevenue.setWeeklyBreakdown(weeklyBreakdown);

        Map<Integer, Double> dayBreakdown = new HashMap<>();
        for (int day = 1; day <= endDate.getDayOfMonth(); day++) {
            LocalDate dayDate = LocalDate.of(year, month, day);
            DailyRevenueDTO dailyRevenue = getDailyRevenue(dayDate);
            dayBreakdown.put(day, dailyRevenue.getTotalRevenue());
        }
        monthlyRevenue.setBreakdownByDayOfMonth(dayBreakdown);

        return monthlyRevenue;
    }

    @Override
    public List<MonthlyRevenueDTO> getMonthlyRevenueRange(int startYear, int startMonth, int endYear, int endMonth) {
        List<MonthlyRevenueDTO> monthlyRevenues = new ArrayList<>();
        YearMonth current = YearMonth.of(startYear, startMonth);
        YearMonth end = YearMonth.of(endYear, endMonth);

        while (!current.isAfter(end)) {
            monthlyRevenues.add(getMonthlyRevenue(current.getYear(), current.getMonthValue()));
            current = current.plusMonths(1);
        }

        return monthlyRevenues;
    }

    @Override
    public YearlyRevenueDTO getYearlyRevenue(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<MonthlyRevenueDTO> monthlyBreakdown = getMonthlyRevenueRange(year, 1, year, 12);

        YearlyRevenueDTO yearlyRevenue = new YearlyRevenueDTO();
        yearlyRevenue.setYear(Year.of(year));
        yearlyRevenue.setYearValue(year);
        yearlyRevenue.setTotalRevenue(monthlyBreakdown.stream().mapToDouble(MonthlyRevenueDTO::getTotalRevenue).sum());
        yearlyRevenue.setTotalOrders(monthlyBreakdown.stream().mapToLong(MonthlyRevenueDTO::getTotalOrders).sum());
        yearlyRevenue.setAverageOrderValue(yearlyRevenue.getTotalOrders() > 0 ?
                yearlyRevenue.getTotalRevenue() / yearlyRevenue.getTotalOrders() : 0.0);
        yearlyRevenue.setCurrency("NGN");
        yearlyRevenue.setMonthlyBreakdown(monthlyBreakdown);
        yearlyRevenue.setBreakdownByMonth(monthlyBreakdown.stream()
                .collect(Collectors.toMap(
                        m -> m.getMonthValue(),
                        MonthlyRevenueDTO::getTotalRevenue
                )));

        YearlyRevenueDTO previousYear = getYearlyRevenue(year - 1);
        if (previousYear.getTotalRevenue() > 0) {
            double growth = ((yearlyRevenue.getTotalRevenue() - previousYear.getTotalRevenue()) / previousYear.getTotalRevenue()) * 100;
            yearlyRevenue.setYearOverYearGrowth(growth);
        }

        Map<Integer, Double> quarterlyBreakdown = new HashMap<>();
        for (int quarter = 1; quarter <= 4; quarter++) {
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = quarter * 3;
            double quarterlyRevenue = monthlyBreakdown.stream()
                    .filter(m -> m.getMonthValue() >= startMonth && m.getMonthValue() <= endMonth)
                    .mapToDouble(MonthlyRevenueDTO::getTotalRevenue)
                    .sum();
            quarterlyBreakdown.put(quarter, quarterlyRevenue);
        }

        double q4Revenue = quarterlyBreakdown.getOrDefault(4, 0.0);
        double q3Revenue = quarterlyBreakdown.getOrDefault(3, 0.0);
        if (q3Revenue > 0) {
            double qoqGrowth = ((q4Revenue - q3Revenue) / q3Revenue) * 100;
            yearlyRevenue.setQuarterOverQuarterGrowth(qoqGrowth);
        }

        return yearlyRevenue;
    }

    @Override
    public List<YearlyRevenueDTO> getYearlyRevenueRange(int startYear, int endYear) {
        List<YearlyRevenueDTO> yearlyRevenues = new ArrayList<>();

        for (int year = startYear; year <= endYear; year++) {
            yearlyRevenues.add(getYearlyRevenue(year));
        }

        return yearlyRevenues;
    }

    @Override
    public RevenueReportDTO getRevenueByState(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findOrdersBetweenDatesAndStatus(startDateTime, endDateTime, OrderStatus.DELIVERED);
        Map<String, Double> revenueByState = getRevenueByState(orders);

        RevenueReportDTO report = new RevenueReportDTO();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setRevenueByState(revenueByState);
        report.setTotalRevenue(revenueByState.values().stream().mapToDouble(Double::doubleValue).sum());
        report.setCurrency("NGN");

        return report;
    }

    @Override
    public RevenueReportDTO getRevenueByVehicleType(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findOrdersBetweenDatesAndStatus(startDateTime, endDateTime, OrderStatus.DELIVERED);
        Map<String, Double> revenueByVehicle = getRevenueByVehicleType(orders);

        RevenueReportDTO report = new RevenueReportDTO();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setRevenueByVehicleType(revenueByVehicle);
        report.setTotalRevenue(revenueByVehicle.values().stream().mapToDouble(Double::doubleValue).sum());
        report.setCurrency("NGN");

        return report;
    }

    @Override
    public RevenueReportDTO getRevenueByPaymentMethod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findOrdersBetweenDatesAndStatus(startDateTime, endDateTime, OrderStatus.DELIVERED);
        Map<String, Double> revenueByPayment = getRevenueByPaymentMethod(orders);

        RevenueReportDTO report = new RevenueReportDTO();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setRevenueByPaymentMethod(revenueByPayment);
        report.setTotalRevenue(revenueByPayment.values().stream().mapToDouble(Double::doubleValue).sum());
        report.setCurrency("NGN");

        return report;
    }

    @Override
    public Double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        Double total = orderRepository.sumTotalPriceBetweenDates(startDateTime, endDateTime);
        return total != null ? total : 0.0;
    }

    @Override
    public Long getTotalOrders(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findOrdersBetweenDatesAndStatus(startDateTime, endDateTime, OrderStatus.DELIVERED);
        return (long) orders.size();
    }

    @Override
    public Double getAverageOrderValue(LocalDate startDate, LocalDate endDate) {
        Long totalOrders = getTotalOrders(startDate, endDate);
        Double totalRevenue = getTotalRevenue(startDate, endDate);

        return totalOrders > 0 ? totalRevenue / totalOrders : 0.0;
    }

    @Override
    public Double getTotalCommission(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        Double total = driverEarningRepository.sumAllCommissionsBetweenDates(startDateTime, endDateTime);
        return total != null ? total : 0.0;
    }

    @Override
    public Double getTotalDriverPayout(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        Double total = driverEarningRepository.sumAllEarningsBetweenDates(startDateTime, endDateTime);
        return total != null ? total : 0.0;
    }

    // ==================== NEW METHODS USING REQUEST DTOs ====================

    @Override
    public DriverEarningsReportDTO getDriverEarnings(DriverEarningsReportRequestDTO request) {
        log.info("Generating earnings report for driver {} from {} to {}",
                request.getDriverId(), request.getStartDate(), request.getEndDate());

        if (!driverRepository.existsById(request.getDriverId())) {
            throw new ResourceNotFoundException("Driver not found: " + request.getDriverId());
        }

        LocalDateTime start = request.getStartDate().atStartOfDay();
        LocalDateTime end = request.getEndDate().atTime(23, 59, 59);

        // Fetch all earnings for this driver in the date range
        List<DriverEarning> earnings = driverEarningRepository.findDriverEarningsBetweenDates(
                request.getDriverId(), start, end);

        double totalEarnings = earnings.stream().mapToDouble(DriverEarning::getAmount).sum();
        double totalCommission = earnings.stream().mapToDouble(DriverEarning::getCommission).sum();
        long totalDeliveries = earnings.size();

        double netEarnings = totalEarnings - totalCommission;
        double avgPerDelivery = totalDeliveries > 0 ? totalEarnings / totalDeliveries : 0.0;

        // Daily breakdown
        Map<String, Double> earningsByDay = new LinkedHashMap<>();
        LocalDate current = request.getStartDate();
        while (!current.isAfter(request.getEndDate())) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(23, 59, 59);
            double dayTotal = earnings.stream()
                    .filter(e -> !e.getEarningDate().isBefore(dayStart) && !e.getEarningDate().isAfter(dayEnd))
                    .mapToDouble(DriverEarning::getAmount)
                    .sum();
            earningsByDay.put(current.toString(), dayTotal);
            current = current.plusDays(1);
        }

        String driverName = driverRepository.findById(request.getDriverId())
                .map(d -> d.getName() )
                .orElse("Unknown");

        return DriverEarningsReportDTO.builder()
                .driverId(request.getDriverId())
                .driverName(driverName)
                .totalEarnings(totalEarnings)
                .totalCommission(totalCommission)
                .netEarnings(netEarnings)
                .totalDeliveries(totalDeliveries)
                .averageEarningPerDelivery(avgPerDelivery)
                .earningsByDay(earningsByDay)
                .currency("NGN")
                .build();
    }

    @Override
    public PaymentReportDTO getPaymentReport(PaymentReportRequestDTO request) {
        log.info("Generating payment report from {} to {} with status {} and method {}",
                request.getStartDate(), request.getEndDate(), request.getPaymentStatus(), request.getPaymentMethod());

        LocalDateTime start = request.getStartDate().atStartOfDay();
        LocalDateTime end = request.getEndDate().atTime(23, 59, 59);

        List<Order> orders = orderRepository.findOrdersBetweenDatesAndStatus(start, end, OrderStatus.DELIVERED);

        if (request.getPaymentStatus() != null) {
            orders = orders.stream()
                    .filter(o -> o.getPaymentStatus() == request.getPaymentStatus())
                    .collect(Collectors.toList());
        }
        if (request.getPaymentMethod() != null) {
            orders = orders.stream()
                    .filter(o -> o.getPaymentMethod() == request.getPaymentMethod())
                    .collect(Collectors.toList());
        }

        Double totalAmount = orders.stream().mapToDouble(Order::getTotalPrice).sum();
        Long totalTransactions = (long) orders.size();

        Map<PaymentStatus, Long> countByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::getPaymentStatus, Collectors.counting()));

        Map<PaymentMethod, Double> amountByMethod = orders.stream()
                .filter(o -> o.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(Order::getPaymentMethod,
                        Collectors.summingDouble(Order::getTotalPrice)));

        long paidCount = orders.stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .count();
        double successRate = totalTransactions > 0 ? (double) paidCount / totalTransactions * 100 : 0.0;

        return PaymentReportDTO.builder()
                .totalAmount(totalAmount)
                .totalTransactions(totalTransactions)
                .transactionCountByStatus(countByStatus)
                .amountByMethod(amountByMethod)
                .successRate(successRate)
                .currency("NGN")
                .build();
    }

    @Override
    public DriverPayoutDTO processDriverPayout(DriverPayoutRequestDTO request) {
        log.info("Processing payout of {} for driver {}", request.getAmount(), request.getDriverId());

        driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + request.getDriverId()));

        // Use sumUnpaidNetEarnings to get the actual unpaid net amount (after commission)
        Double unpaidEarnings = driverEarningRepository.sumUnpaidNetEarnings(request.getDriverId());
        if (unpaidEarnings == null) unpaidEarnings = 0.0;

        if (request.getAmount() > unpaidEarnings) {
            throw new BadRequestException("Insufficient unpaid earnings. Available: " + unpaidEarnings);
        }

        // Get unpaid earnings list using the existing method
        List<DriverEarning> unpaid = driverEarningRepository.findByDriverIdAndPaidFalse(request.getDriverId());
        double remaining = request.getAmount();
        for (DriverEarning earning : unpaid) {
            if (remaining <= 0) break;
            double amountToPay = Math.min(earning.getNetAmount(), remaining);
            earning.setPaid(true);
            earning.setPaidDate(LocalDateTime.now());
            remaining -= amountToPay;
            driverEarningRepository.save(earning);
        }

        String transactionRef = "PAY-" + System.currentTimeMillis();

        return DriverPayoutDTO.builder()
                .payoutId(UUID.randomUUID().toString())
                .driverId(request.getDriverId())
                .amount(request.getAmount())
                .status("SUCCESS")
                .transactionReference(transactionRef)
                .processedAt(LocalDateTime.now())
                .remarks(request.getRemarks())
                .newBalance(unpaidEarnings - request.getAmount())
                .build();
    }

    @Override
    public CommissionReportDTO getCommissionReport(CommissionReportRequestDTO request) {
        log.info("Generating commission report from {} to {}", request.getStartDate(), request.getEndDate());

        LocalDateTime start = request.getStartDate().atStartOfDay();
        LocalDateTime end = request.getEndDate().atTime(23, 59, 59);

        // Total commission
        Double totalCommission = driverEarningRepository.sumAllCommissionsBetweenDates(start, end);
        if (totalCommission == null) totalCommission = 0.0;

        // Total delivered orders in the period
        List<Order> orders = orderRepository.findOrdersBetweenDatesAndStatus(start, end, OrderStatus.DELIVERED);
        Long totalOrders = (long) orders.size();

        double avgCommission = totalOrders > 0 ? totalCommission / totalOrders : 0.0;

        // Commission by day – we can compute per day using the same repository method
        Map<String, Double> commissionByDay = new LinkedHashMap<>();
        LocalDate current = request.getStartDate();
        while (!current.isAfter(request.getEndDate())) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(23, 59, 59);
            Double dayCommission = driverEarningRepository.sumAllCommissionsBetweenDates(dayStart, dayEnd);
            commissionByDay.put(current.toString(), dayCommission != null ? dayCommission : 0.0);
            current = current.plusDays(1);
        }

        // Per‑driver commission breakdown is not directly available from the repository.
        // We could compute it by querying each driver individually, but that would be expensive.
        // To avoid modifying the repository, we leave it empty and log a warning.
        log.warn("Per‑driver commission breakdown is not available; returning empty map.");
        Map<String, Double> commissionByDriver = new HashMap<>();

        return CommissionReportDTO.builder()
                .totalCommission(totalCommission)
                .totalOrders(totalOrders)
                .averageCommissionPerOrder(avgCommission)
                .commissionByDriver(commissionByDriver)
                .commissionByDay(commissionByDay)
                .currency("NGN")
                .build();
    }

    // ==================== PRIVATE HELPERS (unchanged) ====================

    private Map<String, Double> getRevenueByState(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        order -> {
                            String location = order.getDeliveryLocation();
                            String[] parts = location.split(",");
                            return parts.length >= 2 ? parts[1].trim() : "Unknown";
                        },
                        Collectors.summingDouble(Order::getTotalPrice)
                ));
    }

    private Map<String, Double> getRevenueByVehicleType(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getDriver() != null && order.getDriver().getVehicleType() != null ?
                                order.getDriver().getVehicleType().getDisplayName() : "Unknown",
                        Collectors.summingDouble(Order::getTotalPrice)
                ));
    }

    private Map<String, Double> getRevenueByPaymentMethod(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getPaymentMethod() != null ?
                                order.getPaymentMethod().toString() : "Unknown",
                        Collectors.summingDouble(Order::getTotalPrice)
                ));
    }
}