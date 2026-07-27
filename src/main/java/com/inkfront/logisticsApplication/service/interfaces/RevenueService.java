package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.admin.RevenueReportRequestDTO;
import com.inkfront.logisticsApplication.dto.request.revenue.*;
import com.inkfront.logisticsApplication.dto.response.revenue.*;

import java.time.LocalDate;
import java.util.List;

public interface RevenueService {

    // Existing methods (unchanged)
    RevenueReportDTO generateRevenueReport(RevenueReportRequestDTO request);

    DailyRevenueDTO getDailyRevenue(LocalDate date);

    List<DailyRevenueDTO> getDailyRevenueRange(LocalDate startDate, LocalDate endDate);

    WeeklyRevenueDTO getWeeklyRevenue(LocalDate date);

    List<WeeklyRevenueDTO> getWeeklyRevenueRange(LocalDate startDate, LocalDate endDate);

    MonthlyRevenueDTO getMonthlyRevenue(int year, int month);

    List<MonthlyRevenueDTO> getMonthlyRevenueRange(int startYear, int startMonth, int endYear, int endMonth);

    YearlyRevenueDTO getYearlyRevenue(int year);

    List<YearlyRevenueDTO> getYearlyRevenueRange(int startYear, int endYear);

    RevenueReportDTO getRevenueByState(LocalDate startDate, LocalDate endDate);

    RevenueReportDTO getRevenueByVehicleType(LocalDate startDate, LocalDate endDate);

    RevenueReportDTO getRevenueByPaymentMethod(LocalDate startDate, LocalDate endDate);

    Double getTotalRevenue(LocalDate startDate, LocalDate endDate);

    Long getTotalOrders(LocalDate startDate, LocalDate endDate);

    Double getAverageOrderValue(LocalDate startDate, LocalDate endDate);

    Double getTotalCommission(LocalDate startDate, LocalDate endDate);

    Double getTotalDriverPayout(LocalDate startDate, LocalDate endDate);

    // --- NEW METHODS USING REQUEST DTOs ---

    DriverEarningsReportDTO getDriverEarnings(DriverEarningsReportRequestDTO request);

    PaymentReportDTO getPaymentReport(PaymentReportRequestDTO request);

    DriverPayoutDTO processDriverPayout(DriverPayoutRequestDTO request);

    CommissionReportDTO getCommissionReport(CommissionReportRequestDTO request);
}