package com.inkfront.logisticsApplication.util.report;

import com.inkfront.logisticsApplication.dto.response.report.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExcelReportExporter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== REVENUE REPORT ====================

    public Resource exportRevenueReport(RevenueReportDTO report) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet summarySheet = workbook.createSheet("Summary");
            createRevenueSummary(summarySheet, report);

            Sheet revenueSheet = workbook.createSheet("Revenue Details");
            createRevenueDetails(revenueSheet, report);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generating Excel revenue report", e);
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    // ==================== ORDER REPORT ====================

    public Resource exportOrderReport(OrderReportDTO report) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet summarySheet = workbook.createSheet("Summary");
            createOrderSummary(summarySheet, report);

            Sheet statusSheet = workbook.createSheet("Orders by Status");
            createMapTable(statusSheet, report.getOrdersByStatus(), "Status", "Count");

            Sheet locationSheet = workbook.createSheet("Orders by Location");
            createMapTable(locationSheet, report.getOrdersByLocation(), "Location", "Count");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generating Excel order report", e);
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    // ==================== DRIVER REPORT ====================

    public Resource exportDriverReport(DriverReportDTO report) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet summarySheet = workbook.createSheet("Summary");
            createDriverSummary(summarySheet, report);

            Sheet performanceSheet = workbook.createSheet("Driver Performance");
            createMapTable(performanceSheet, report.getDriverPerformance(), "Driver ID", "Deliveries");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generating Excel driver report", e);
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    // ==================== CUSTOMER REPORT ====================

    public Resource exportCustomerReport(CustomerReportDTO report) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet summarySheet = workbook.createSheet("Summary");
            createCustomerSummary(summarySheet, report);

            Sheet topSheet = workbook.createSheet("Top Customers");
            int rowNum = 0;
            Row header = topSheet.createRow(rowNum++);
            header.createCell(0).setCellValue("Customer");
            for (String customer : report.getTopCustomers()) {
                Row row = topSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(customer);
            }
            topSheet.autoSizeColumn(0);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generating Excel customer report", e);
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    // ==================== HELPER METHODS ====================

    private void createRevenueSummary(Sheet sheet, RevenueReportDTO report) {
        int rowNum = 0;
        Row header = sheet.createRow(rowNum++);
        createHeaderCell(header, 0, "Metric");
        createHeaderCell(header, 1, "Value");

        rowNum = addStatRow(sheet, rowNum, "Total Revenue", report.getTotalRevenue());
        rowNum = addStatRow(sheet, rowNum, "Average Order Value", report.getAverageOrderValue());
        rowNum = addStatRow(sheet, rowNum, "Revenue Growth (%)", report.getRevenueGrowth());
        rowNum = addStatRow(sheet, rowNum, "Pending Revenue", report.getPendingRevenue());
        rowNum = addStatRow(sheet, rowNum, "Cancelled Revenue", report.getCancelledRevenue());

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createRevenueDetails(Sheet sheet, RevenueReportDTO report) {
        int rowNum = 0;
        Row header = sheet.createRow(rowNum++);
        createHeaderCell(header, 0, "Date");
        createHeaderCell(header, 1, "Revenue");
        for (Map.Entry<LocalDate, Double> entry : report.getRevenueByDay().entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey().format(DATE_FORMAT));
            row.createCell(1).setCellValue(entry.getValue());
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createOrderSummary(Sheet sheet, OrderReportDTO report) {
        int rowNum = 0;
        Row header = sheet.createRow(rowNum++);
        createHeaderCell(header, 0, "Metric");
        createHeaderCell(header, 1, "Value");

        rowNum = addStatRow(sheet, rowNum, "Total Orders", report.getTotalOrders());
        rowNum = addStatRow(sheet, rowNum, "Completed", report.getCompletedOrders());
        rowNum = addStatRow(sheet, rowNum, "Cancelled", report.getCancelledOrders());
        rowNum = addStatRow(sheet, rowNum, "In Transit", report.getInTransitOrders());
        rowNum = addStatRow(sheet, rowNum, "Average Delivery Time (min)", report.getAverageDeliveryTimeMinutes());
        rowNum = addStatRow(sheet, rowNum, "Average Distance (km)", report.getAverageDistanceKm());
        rowNum = addStatRow(sheet, rowNum, "Most Requested Vehicle", report.getMostRequestedVehicle());

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createDriverSummary(Sheet sheet, DriverReportDTO report) {
        int rowNum = 0;
        Row header = sheet.createRow(rowNum++);
        createHeaderCell(header, 0, "Metric");
        createHeaderCell(header, 1, "Value");

        rowNum = addStatRow(sheet, rowNum, "Total Drivers", report.getTotalDrivers());
        rowNum = addStatRow(sheet, rowNum, "Available", report.getAvailableDrivers());
        rowNum = addStatRow(sheet, rowNum, "Busy", report.getBusyDrivers());
        rowNum = addStatRow(sheet, rowNum, "Completed Deliveries", report.getCompletedDeliveries());
        rowNum = addStatRow(sheet, rowNum, "Average Rating", report.getAverageRating());
        rowNum = addStatRow(sheet, rowNum, "Total Distance Covered (km)", report.getTotalDistanceCoveredKm());
        rowNum = addStatRow(sheet, rowNum, "Revenue Generated", report.getRevenueGenerated());

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createCustomerSummary(Sheet sheet, CustomerReportDTO report) {
        int rowNum = 0;
        Row header = sheet.createRow(rowNum++);
        createHeaderCell(header, 0, "Metric");
        createHeaderCell(header, 1, "Value");

        rowNum = addStatRow(sheet, rowNum, "Total Customers", report.getTotalCustomers());
        rowNum = addStatRow(sheet, rowNum, "New Customers", report.getNewCustomers());
        rowNum = addStatRow(sheet, rowNum, "Returning Customers", report.getReturningCustomers());
        rowNum = addStatRow(sheet, rowNum, "Average Spend", report.getAverageSpend());
        rowNum = addStatRow(sheet, rowNum, "Completed Orders", report.getCompletedOrders());
        rowNum = addStatRow(sheet, rowNum, "Cancelled Orders", report.getCancelledOrders());

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createMapTable(Sheet sheet, Map<String, Long> data, String keyHeader, String valueHeader) {
        int rowNum = 0;
        Row header = sheet.createRow(rowNum++);
        createHeaderCell(header, 0, keyHeader);
        createHeaderCell(header, 1, valueHeader);
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createHeaderCell(Row row, int col, String value) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        CellStyle style = row.getSheet().getWorkbook().createCellStyle();
        Font font = row.getSheet().getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    private int addStatRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(label);
        if (value instanceof Number) {
            row.createCell(1).setCellValue(((Number) value).doubleValue());
        } else {
            row.createCell(1).setCellValue(value != null ? value.toString() : "");
        }
        return rowNum;
    }

    private int addStatRow(Sheet sheet, int rowNum, String label, double value) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
        return rowNum;
    }

    private int addStatRow(Sheet sheet, int rowNum, String label, long value) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
        return rowNum;
    }
}