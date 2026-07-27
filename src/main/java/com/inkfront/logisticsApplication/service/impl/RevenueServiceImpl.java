package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.RevenueReport;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.ReportPeriod;
import com.inkfront.logisticsApplication.dto.request.admin.RevenueReportRequestDTO;
import com.inkfront.logisticsApplication.dto.response.revenue.*;
import com.inkfront.logisticsApplication.exception.BadRequestException;
import com.inkfront.logisticsApplication.mapper.RevenueReportMapper;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.RevenueReportRepository;
import com.inkfront.logisticsApplication.repository.DriverEarningRepository;
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
    private final RevenueReportMapper revenueReportMapper;

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

        // Convert to LocalDateTime for query
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Get orders for the period
        List<Order> orders = orderRepository.findOrdersBetweenDatesAndStatus(
                startDateTime, endDateTime, OrderStatus.DELIVERED
        );

        // Calculate revenue metrics
        double totalRevenue = orders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();

        long totalOrders = orders.size();
        double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;

        // Get commission and driver payouts
        Double totalCommission = driverEarningRepository.sumAllCommissionsBetweenDates(startDateTime, endDateTime);
        Double totalDriverPayout = driverEarningRepository.sumAllEarningsBetweenDates(startDateTime, endDateTime);

        totalCommission = totalCommission != null ? totalCommission : 0.0;
        totalDriverPayout = totalDriverPayout != null ? totalDriverPayout : 0.0;

        // Create report
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

        // Add breakdowns if requested
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

        // Breakdown by day of month
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

        // Calculate year over year growth
        YearlyRevenueDTO previousYear = getYearlyRevenue(year - 1);
        if (previousYear.getTotalRevenue() > 0) {
            double growth = ((yearlyRevenue.getTotalRevenue() - previousYear.getTotalRevenue()) / previousYear.getTotalRevenue()) * 100;
            yearlyRevenue.setYearOverYearGrowth(growth);
        }

        // Calculate quarter over quarter growth
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

    private Map<String, Double> getRevenueByState(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        order -> {
                            String location = order.getDeliveryLocation();
                            // Extract state from location (assuming format: "City, State, Country")
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