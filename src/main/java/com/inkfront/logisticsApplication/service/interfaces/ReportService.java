package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.admin.RevenueReportRequestDTO;
import com.inkfront.logisticsApplication.dto.response.revenue.RevenueReportDTO;

import java.io.ByteArrayOutputStream;

public interface ReportService {

    RevenueReportDTO generateRevenueReport(RevenueReportRequestDTO request);

    ByteArrayOutputStream exportReportToPDF(RevenueReportDTO report);

    ByteArrayOutputStream exportReportToExcel(RevenueReportDTO report);

    ByteArrayOutputStream exportReportToCSV(RevenueReportDTO report);

    void scheduleDailyReportGeneration();

    void scheduleWeeklyReportGeneration();

    void scheduleMonthlyReportGeneration();

    void scheduleYearlyReportGeneration();
}