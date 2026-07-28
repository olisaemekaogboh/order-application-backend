package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.report.*;
import com.inkfront.logisticsApplication.dto.response.report.*;
import org.springframework.core.io.Resource;

public interface ReportService {

    RevenueReportDTO generateRevenueReport(RevenueReportRequestDTO request);

    OrderReportDTO generateOrderReport(OrderReportRequestDTO request);

    DriverReportDTO generateDriverReport(DriverReportRequestDTO request);

    CustomerReportDTO generateCustomerReport(CustomerReportRequestDTO request);

    DeliveryPerformanceReportDTO generateDeliveryReport(DeliveryReportRequestDTO request);

    DashboardAnalyticsReportDTO generateDashboardAnalytics();

    Resource exportRevenueReportToPdf(RevenueReportRequestDTO request);

    Resource exportRevenueReportToExcel(RevenueReportRequestDTO request);

    Resource exportRevenueReportToCsv(RevenueReportRequestDTO request);

    Resource exportOrderReportToPdf(OrderReportRequestDTO request);

    Resource exportOrderReportToExcel(OrderReportRequestDTO request);

    Resource exportOrderReportToCsv(OrderReportRequestDTO request);

    Resource exportDriverReportToPdf(DriverReportRequestDTO request);

    Resource exportDriverReportToExcel(DriverReportRequestDTO request);

    Resource exportDriverReportToCsv(DriverReportRequestDTO request);

    Resource exportCustomerReportToPdf(CustomerReportRequestDTO request);

    Resource exportCustomerReportToExcel(CustomerReportRequestDTO request);

    Resource exportCustomerReportToCsv(CustomerReportRequestDTO request);
}