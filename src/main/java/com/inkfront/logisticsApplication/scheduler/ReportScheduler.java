package com.inkfront.logisticsApplication.scheduler;

import com.inkfront.logisticsApplication.dto.request.report.*;
import com.inkfront.logisticsApplication.service.interfaces.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ReportScheduler {

    private final ReportService reportService;

    @Scheduled(cron = "0 0 23 * * ?") // daily at 23:00
    public void generateDailyReport() {
        log.info("Generating daily report...");
        LocalDate today = LocalDate.now();
        RevenueReportRequestDTO revenueRequest = RevenueReportRequestDTO.builder()
                .startDate(today)
                .endDate(today)
                .build();
        reportService.generateRevenueReport(revenueRequest);
        // Optionally email
    }

    @Scheduled(cron = "0 0 22 ? * SUN") // weekly on Sunday at 22:00
    public void generateWeeklyReport() {
        log.info("Generating weekly report...");
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);
        RevenueReportRequestDTO request = RevenueReportRequestDTO.builder()
                .startDate(start)
                .endDate(end)
                .build();
        reportService.generateRevenueReport(request);
    }

    @Scheduled(cron = "0 0 21 1 * ?") // monthly on 1st at 21:00
    public void generateMonthlyReport() {
        log.info("Generating monthly report...");
        LocalDate end = LocalDate.now();
        LocalDate start = end.withDayOfMonth(1);
        RevenueReportRequestDTO request = RevenueReportRequestDTO.builder()
                .startDate(start)
                .endDate(end)
                .build();
        reportService.generateRevenueReport(request);
    }

    @Scheduled(cron = "0 0 20 1 1 ?") // yearly on Jan 1st at 20:00
    public void generateYearlyReport() {
        log.info("Generating yearly report...");
        LocalDate end = LocalDate.now();
        LocalDate start = end.withDayOfYear(1);
        RevenueReportRequestDTO request = RevenueReportRequestDTO.builder()
                .startDate(start)
                .endDate(end)
                .build();
        reportService.generateRevenueReport(request);
    }
}