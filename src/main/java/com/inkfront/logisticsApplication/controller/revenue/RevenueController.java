package com.inkfront.logisticsApplication.controller.revenue;

import com.inkfront.logisticsApplication.dto.request.admin.RevenueReportRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.revenue.*;
import com.inkfront.logisticsApplication.service.interfaces.RevenueService;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/revenue")
@RequiredArgsConstructor
@Tag(name = "Revenue Management", description = "Revenue management endpoints")
public class RevenueController {

    private final RevenueService revenueService;

    @PostMapping("/report/generate")
    @Operation(summary = "Generate revenue report")
    public ResponseEntity<ApiResponseDTO<RevenueReportDTO>> generateRevenueReport(
            @Valid @RequestBody RevenueReportRequestDTO request) {
        log.info("Generate revenue report request for period: {}", request.getPeriod());
        RevenueReportDTO response = revenueService.generateRevenueReport(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.REPORT_GENERATED, response));
    }

    @GetMapping("/daily")
    @Operation(summary = "Get daily revenue")
    public ResponseEntity<ApiResponseDTO<DailyRevenueDTO>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Get daily revenue request for date: {}", date);
        DailyRevenueDTO response = revenueService.getDailyRevenue(date);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/daily/range")
    @Operation(summary = "Get daily revenue range")
    public ResponseEntity<ApiResponseDTO<List<DailyRevenueDTO>>> getDailyRevenueRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get daily revenue range request from: {} to: {}", startDate, endDate);
        List<DailyRevenueDTO> response = revenueService.getDailyRevenueRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/weekly")
    @Operation(summary = "Get weekly revenue")
    public ResponseEntity<ApiResponseDTO<WeeklyRevenueDTO>> getWeeklyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Get weekly revenue request for date: {}", date);
        WeeklyRevenueDTO response = revenueService.getWeeklyRevenue(date);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/weekly/range")
    @Operation(summary = "Get weekly revenue range")
    public ResponseEntity<ApiResponseDTO<List<WeeklyRevenueDTO>>> getWeeklyRevenueRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get weekly revenue range request from: {} to: {}", startDate, endDate);
        List<WeeklyRevenueDTO> response = revenueService.getWeeklyRevenueRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly revenue")
    public ResponseEntity<ApiResponseDTO<MonthlyRevenueDTO>> getMonthlyRevenue(
            @RequestParam int year,
            @RequestParam int month) {
        log.info("Get monthly revenue request for: {}-{}", year, month);
        MonthlyRevenueDTO response = revenueService.getMonthlyRevenue(year, month);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/monthly/range")
    @Operation(summary = "Get monthly revenue range")
    public ResponseEntity<ApiResponseDTO<List<MonthlyRevenueDTO>>> getMonthlyRevenueRange(
            @RequestParam int startYear,
            @RequestParam int startMonth,
            @RequestParam int endYear,
            @RequestParam int endMonth) {
        log.info("Get monthly revenue range request from: {}-{} to: {}-{}", startYear, startMonth, endYear, endMonth);
        List<MonthlyRevenueDTO> response = revenueService.getMonthlyRevenueRange(startYear, startMonth, endYear, endMonth);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/yearly")
    @Operation(summary = "Get yearly revenue")
    public ResponseEntity<ApiResponseDTO<YearlyRevenueDTO>> getYearlyRevenue(@RequestParam int year) {
        log.info("Get yearly revenue request for: {}", year);
        YearlyRevenueDTO response = revenueService.getYearlyRevenue(year);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/yearly/range")
    @Operation(summary = "Get yearly revenue range")
    public ResponseEntity<ApiResponseDTO<List<YearlyRevenueDTO>>> getYearlyRevenueRange(
            @RequestParam int startYear,
            @RequestParam int endYear) {
        log.info("Get yearly revenue range request from: {} to: {}", startYear, endYear);
        List<YearlyRevenueDTO> response = revenueService.getYearlyRevenueRange(startYear, endYear);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/by-state")
    @Operation(summary = "Get revenue by state")
    public ResponseEntity<ApiResponseDTO<RevenueReportDTO>> getRevenueByState(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get revenue by state request from: {} to: {}", startDate, endDate);
        RevenueReportDTO response = revenueService.getRevenueByState(startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/by-vehicle")
    @Operation(summary = "Get revenue by vehicle type")
    public ResponseEntity<ApiResponseDTO<RevenueReportDTO>> getRevenueByVehicleType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get revenue by vehicle type request from: {} to: {}", startDate, endDate);
        RevenueReportDTO response = revenueService.getRevenueByVehicleType(startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/by-payment")
    @Operation(summary = "Get revenue by payment method")
    public ResponseEntity<ApiResponseDTO<RevenueReportDTO>> getRevenueByPaymentMethod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get revenue by payment method request from: {} to: {}", startDate, endDate);
        RevenueReportDTO response = revenueService.getRevenueByPaymentMethod(startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/total")
    @Operation(summary = "Get total revenue")
    public ResponseEntity<ApiResponseDTO<Double>> getTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get total revenue request from: {} to: {}", startDate, endDate);
        Double response = revenueService.getTotalRevenue(startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success("Total revenue retrieved", response));
    }

    @GetMapping("/total-orders")
    @Operation(summary = "Get total orders")
    public ResponseEntity<ApiResponseDTO<Long>> getTotalOrders(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get total orders request from: {} to: {}", startDate, endDate);
        Long response = revenueService.getTotalOrders(startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success("Total orders retrieved", response));
    }

    @GetMapping("/average-order")
    @Operation(summary = "Get average order value")
    public ResponseEntity<ApiResponseDTO<Double>> getAverageOrderValue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get average order value request from: {} to: {}", startDate, endDate);
        Double response = revenueService.getAverageOrderValue(startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success("Average order value retrieved", response));
    }

    @GetMapping("/commission")
    @Operation(summary = "Get total commission")
    public ResponseEntity<ApiResponseDTO<Double>> getTotalCommission(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get total commission request from: {} to: {}", startDate, endDate);
        Double response = revenueService.getTotalCommission(startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success("Total commission retrieved", response));
    }

    @GetMapping("/driver-payout")
    @Operation(summary = "Get total driver payout")
    public ResponseEntity<ApiResponseDTO<Double>> getTotalDriverPayout(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get total driver payout request from: {} to: {}", startDate, endDate);
        Double response = revenueService.getTotalDriverPayout(startDate, endDate);
        return ResponseEntity.ok(ApiResponseDTO.success("Total driver payout retrieved", response));
    }
}