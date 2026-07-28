package com.inkfront.logisticsApplication.controller.report;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.report.*;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.report.*;
import com.inkfront.logisticsApplication.service.interfaces.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "Reporting and Analytics APIs")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== GENERATE REPORTS (JSON) ====================

    @PostMapping("/revenue")
    @Operation(summary = "Generate Revenue Report")
    public ResponseEntity<ApiResponseDTO<RevenueReportDTO>> generateRevenueReport(
            @Valid @RequestBody RevenueReportRequestDTO request) {
        log.info("Generating revenue report from {} to {}", request.getStartDate(), request.getEndDate());
        RevenueReportDTO report = reportService.generateRevenueReport(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, report));
    }

    @PostMapping("/orders")
    @Operation(summary = "Generate Order Report")
    public ResponseEntity<ApiResponseDTO<OrderReportDTO>> generateOrderReport(
            @Valid @RequestBody OrderReportRequestDTO request) {
        log.info("Generating order report from {} to {}", request.getStartDate(), request.getEndDate());
        OrderReportDTO report = reportService.generateOrderReport(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, report));
    }

    @PostMapping("/drivers")
    @Operation(summary = "Generate Driver Report")
    public ResponseEntity<ApiResponseDTO<DriverReportDTO>> generateDriverReport(
            @Valid @RequestBody DriverReportRequestDTO request) {
        log.info("Generating driver report from {} to {}", request.getStartDate(), request.getEndDate());
        DriverReportDTO report = reportService.generateDriverReport(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, report));
    }

    @PostMapping("/customers")
    @Operation(summary = "Generate Customer Report")
    public ResponseEntity<ApiResponseDTO<CustomerReportDTO>> generateCustomerReport(
            @Valid @RequestBody CustomerReportRequestDTO request) {
        log.info("Generating customer report from {} to {}", request.getStartDate(), request.getEndDate());
        CustomerReportDTO report = reportService.generateCustomerReport(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, report));
    }

    @PostMapping("/delivery")
    @Operation(summary = "Generate Delivery Performance Report")
    public ResponseEntity<ApiResponseDTO<DeliveryPerformanceReportDTO>> generateDeliveryReport(
            @Valid @RequestBody DeliveryReportRequestDTO request) {
        log.info("Generating delivery performance report from {} to {}", request.getStartDate(), request.getEndDate());
        DeliveryPerformanceReportDTO report = reportService.generateDeliveryReport(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, report));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Generate Dashboard Analytics")
    public ResponseEntity<ApiResponseDTO<DashboardAnalyticsReportDTO>> generateDashboardAnalytics() {
        log.info("Generating dashboard analytics");
        DashboardAnalyticsReportDTO report = reportService.generateDashboardAnalytics();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, report));
    }

    // ==================== DOWNLOAD ENDPOINTS ====================

    // Revenue
    @PostMapping("/revenue/pdf")
    @Operation(summary = "Download Revenue Report as PDF")
    public ResponseEntity<Resource> downloadRevenuePdf(@Valid @RequestBody RevenueReportRequestDTO request) {
        Resource resource = reportService.exportRevenueReportToPdf(request);
        return buildPdfResponse(resource, "Revenue_Report", request.getStartDate(), request.getEndDate());
    }

    @PostMapping("/revenue/excel")
    @Operation(summary = "Download Revenue Report as Excel")
    public ResponseEntity<Resource> downloadRevenueExcel(@Valid @RequestBody RevenueReportRequestDTO request) {
        Resource resource = reportService.exportRevenueReportToExcel(request);
        return buildExcelResponse(resource, "Revenue_Report", request.getStartDate(), request.getEndDate());
    }

    @PostMapping("/revenue/csv")
    @Operation(summary = "Download Revenue Report as CSV")
    public ResponseEntity<Resource> downloadRevenueCsv(@Valid @RequestBody RevenueReportRequestDTO request) {
        Resource resource = reportService.exportRevenueReportToCsv(request);
        return buildCsvResponse(resource, "Revenue_Report", request.getStartDate(), request.getEndDate());
    }

    // Orders
    @PostMapping("/orders/pdf")
    @Operation(summary = "Download Order Report as PDF")
    public ResponseEntity<Resource> downloadOrdersPdf(@Valid @RequestBody OrderReportRequestDTO request) {
        Resource resource = reportService.exportOrderReportToPdf(request);
        return buildPdfResponse(resource, "Orders_Report", request.getStartDate(), request.getEndDate());
    }

    @PostMapping("/orders/excel")
    @Operation(summary = "Download Order Report as Excel")
    public ResponseEntity<Resource> downloadOrdersExcel(@Valid @RequestBody OrderReportRequestDTO request) {
        Resource resource = reportService.exportOrderReportToExcel(request);
        return buildExcelResponse(resource, "Orders_Report", request.getStartDate(), request.getEndDate());
    }

    @PostMapping("/orders/csv")
    @Operation(summary = "Download Order Report as CSV")
    public ResponseEntity<Resource> downloadOrdersCsv(@Valid @RequestBody OrderReportRequestDTO request) {
        Resource resource = reportService.exportOrderReportToCsv(request);
        return buildCsvResponse(resource, "Orders_Report", request.getStartDate(), request.getEndDate());
    }

    // Drivers
    @PostMapping("/drivers/pdf")
    @Operation(summary = "Download Driver Report as PDF")
    public ResponseEntity<Resource> downloadDriversPdf(@Valid @RequestBody DriverReportRequestDTO request) {
        Resource resource = reportService.exportDriverReportToPdf(request);
        return buildPdfResponse(resource, "Drivers_Report", request.getStartDate(), request.getEndDate());
    }

    @PostMapping("/drivers/excel")
    @Operation(summary = "Download Driver Report as Excel")
    public ResponseEntity<Resource> downloadDriversExcel(@Valid @RequestBody DriverReportRequestDTO request) {
        Resource resource = reportService.exportDriverReportToExcel(request);
        return buildExcelResponse(resource, "Drivers_Report", request.getStartDate(), request.getEndDate());
    }

    @PostMapping("/drivers/csv")
    @Operation(summary = "Download Driver Report as CSV")
    public ResponseEntity<Resource> downloadDriversCsv(@Valid @RequestBody DriverReportRequestDTO request) {
        Resource resource = reportService.exportDriverReportToCsv(request);
        return buildCsvResponse(resource, "Drivers_Report", request.getStartDate(), request.getEndDate());
    }

    // Customers
    @PostMapping("/customers/pdf")
    @Operation(summary = "Download Customer Report as PDF")
    public ResponseEntity<Resource> downloadCustomersPdf(@Valid @RequestBody CustomerReportRequestDTO request) {
        Resource resource = reportService.exportCustomerReportToPdf(request);
        return buildPdfResponse(resource, "Customers_Report", request.getStartDate(), request.getEndDate());
    }

    @PostMapping("/customers/excel")
    @Operation(summary = "Download Customer Report as Excel")
    public ResponseEntity<Resource> downloadCustomersExcel(@Valid @RequestBody CustomerReportRequestDTO request) {
        Resource resource = reportService.exportCustomerReportToExcel(request);
        return buildExcelResponse(resource, "Customers_Report", request.getStartDate(), request.getEndDate());
    }

    @PostMapping("/customers/csv")
    @Operation(summary = "Download Customer Report as CSV")
    public ResponseEntity<Resource> downloadCustomersCsv(@Valid @RequestBody CustomerReportRequestDTO request) {
        Resource resource = reportService.exportCustomerReportToCsv(request);
        return buildCsvResponse(resource, "Customers_Report", request.getStartDate(), request.getEndDate());
    }

    // ==================== RESPONSE BUILDERS ====================

    private ResponseEntity<Resource> buildPdfResponse(Resource resource, String baseName,
                                                      LocalDate start, LocalDate end) {
        String filename = baseName + "_" + start.format(FILE_DATE_FORMAT) + "_to_" + end.format(FILE_DATE_FORMAT) + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    private ResponseEntity<Resource> buildExcelResponse(Resource resource, String baseName,
                                                        LocalDate start, LocalDate end) {
        String filename = baseName + "_" + start.format(FILE_DATE_FORMAT) + "_to_" + end.format(FILE_DATE_FORMAT) + ".xlsx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    private ResponseEntity<Resource> buildCsvResponse(Resource resource, String baseName,
                                                      LocalDate start, LocalDate end) {
        String filename = baseName + "_" + start.format(FILE_DATE_FORMAT) + "_to_" + end.format(FILE_DATE_FORMAT) + ".csv";
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}