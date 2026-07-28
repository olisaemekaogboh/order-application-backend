package com.inkfront.logisticsApplication.util.report;

import com.inkfront.logisticsApplication.dto.response.report.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfReportExporter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD);
    private static final Font HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);

    // ==================== REVENUE REPORT ====================

    public Resource exportRevenueReport(RevenueReportDTO report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            addTitle(document, "Revenue Report");
            addSummary(document, report.getSummary());
            addRevenueStats(document, report);
            addRevenueTable(document, "Revenue by Day", report.getRevenueByDay());
            addRevenueTable(document, "Revenue by Week", report.getRevenueByWeek());
            addRevenueTable(document, "Revenue by Month", report.getRevenueByMonth());

            document.close();
            return new ByteArrayResource(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generating PDF revenue report", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    // ==================== ORDER REPORT ====================

    public Resource exportOrderReport(OrderReportDTO report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            addTitle(document, "Order Report");
            addSummary(document, report.getSummary());
            addOrderStats(document, report);
            addTableFromMap(document, "Orders by Status", report.getOrdersByStatus());
            addTableFromMap(document, "Orders by Location", report.getOrdersByLocation());

            document.close();
            return new ByteArrayResource(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generating PDF order report", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    // ==================== DRIVER REPORT ====================

    public Resource exportDriverReport(DriverReportDTO report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            addTitle(document, "Driver Report");
            addSummary(document, report.getSummary());
            addDriverStats(document, report);
            addTableFromMap(document, "Driver Performance (Deliveries)", report.getDriverPerformance());

            document.close();
            return new ByteArrayResource(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generating PDF driver report", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    // ==================== CUSTOMER REPORT ====================

    public Resource exportCustomerReport(CustomerReportDTO report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            addTitle(document, "Customer Report");
            addSummary(document, report.getSummary());
            addCustomerStats(document, report);

            // Top customers list
            document.add(new Paragraph("Top Customers", HEADING_FONT));
            for (String customer : report.getTopCustomers()) {
                document.add(new Paragraph("• " + customer, NORMAL_FONT));
            }
            document.add(Chunk.NEWLINE);

            document.close();
            return new ByteArrayResource(out.toByteArray());
        } catch (Exception e) {
            log.error("Error generating PDF customer report", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    // ==================== HELPER METHODS ====================

    private void addTitle(Document document, String title) throws DocumentException {
        Paragraph titlePara = new Paragraph(title, TITLE_FONT);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);
    }

    private void addSummary(Document document, ReportSummaryDTO summary) throws DocumentException {
        if (summary == null) return;
        document.add(new Paragraph("Generated on: " + summary.getGeneratedAt().format(DATE_FORMAT), NORMAL_FONT));
        document.add(new Paragraph("Period: " + summary.getStartDate() + " to " + summary.getEndDate(), NORMAL_FONT));
        document.add(new Paragraph("Generated by: " + summary.getGeneratedBy(), NORMAL_FONT));
        document.add(Chunk.NEWLINE);
    }

    private void addRevenueStats(Document document, RevenueReportDTO report) throws DocumentException {
        document.add(new Paragraph("Revenue Summary", HEADING_FONT));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        addStatRow(table, "Total Revenue", formatCurrency(report.getTotalRevenue()));
        addStatRow(table, "Average Order Value", formatCurrency(report.getAverageOrderValue()));
        addStatRow(table, "Revenue Growth", String.format("%.2f%%", report.getRevenueGrowth()));
        addStatRow(table, "Pending Revenue", formatCurrency(report.getPendingRevenue()));
        addStatRow(table, "Cancelled Revenue", formatCurrency(report.getCancelledRevenue()));
        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addOrderStats(Document document, OrderReportDTO report) throws DocumentException {
        document.add(new Paragraph("Order Summary", HEADING_FONT));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        addStatRow(table, "Total Orders", String.valueOf(report.getTotalOrders()));
        addStatRow(table, "Completed", String.valueOf(report.getCompletedOrders()));
        addStatRow(table, "Cancelled", String.valueOf(report.getCancelledOrders()));
        addStatRow(table, "In Transit", String.valueOf(report.getInTransitOrders()));
        addStatRow(table, "Average Delivery Time (min)", String.format("%.1f", report.getAverageDeliveryTimeMinutes()));
        addStatRow(table, "Average Distance (km)", String.format("%.2f", report.getAverageDistanceKm()));
        addStatRow(table, "Most Requested Vehicle", report.getMostRequestedVehicle());
        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addDriverStats(Document document, DriverReportDTO report) throws DocumentException {
        document.add(new Paragraph("Driver Summary", HEADING_FONT));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        addStatRow(table, "Total Drivers", String.valueOf(report.getTotalDrivers()));
        addStatRow(table, "Available", String.valueOf(report.getAvailableDrivers()));
        addStatRow(table, "Busy", String.valueOf(report.getBusyDrivers()));
        addStatRow(table, "Completed Deliveries", String.valueOf(report.getCompletedDeliveries()));
        addStatRow(table, "Average Rating", String.format("%.1f", report.getAverageRating()));
        addStatRow(table, "Total Distance Covered (km)", String.format("%.2f", report.getTotalDistanceCoveredKm()));
        addStatRow(table, "Revenue Generated", formatCurrency(report.getRevenueGenerated()));
        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addCustomerStats(Document document, CustomerReportDTO report) throws DocumentException {
        document.add(new Paragraph("Customer Summary", HEADING_FONT));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        addStatRow(table, "Total Customers", String.valueOf(report.getTotalCustomers()));
        addStatRow(table, "New Customers", String.valueOf(report.getNewCustomers()));
        addStatRow(table, "Returning Customers", String.valueOf(report.getReturningCustomers()));
        addStatRow(table, "Average Spend", formatCurrency(report.getAverageSpend()));
        addStatRow(table, "Completed Orders", String.valueOf(report.getCompletedOrders()));
        addStatRow(table, "Cancelled Orders", String.valueOf(report.getCancelledOrders()));
        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addRevenueTable(Document document, String title, Map<?, Double> data) throws DocumentException {
        if (data == null || data.isEmpty()) return;
        document.add(new Paragraph(title, HEADING_FONT));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell(createHeaderCell("Period"));
        table.addCell(createHeaderCell("Revenue"));
        data.forEach((key, value) -> {
            table.addCell(key.toString());
            table.addCell(formatCurrency(value));
        });
        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addTableFromMap(Document document, String title, Map<String, Long> data) throws DocumentException {
        if (data == null || data.isEmpty()) return;
        document.add(new Paragraph(title, HEADING_FONT));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell(createHeaderCell("Category"));
        table.addCell(createHeaderCell("Count"));
        data.forEach((key, value) -> {
            table.addCell(key);
            table.addCell(String.valueOf(value));
        });
        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addStatRow(PdfPTable table, String label, String value) {
        table.addCell(createLabelCell(label));
        table.addCell(createValueCell(value));
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        cell.setBackgroundColor(Color.BLACK);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell createLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private PdfPCell createValueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private String formatCurrency(Double amount) {
        if (amount == null) return "₦0.00";
        return "₦" + String.format("%,.2f", amount);
    }
}