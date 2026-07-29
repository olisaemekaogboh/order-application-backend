package com.inkfront.logisticsApplication.controller.dashboard;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.dashboard.*;
import com.inkfront.logisticsApplication.dto.response.admin.DashboardStatsDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.dashboard.*;
import com.inkfront.logisticsApplication.dto.response.review.ReviewAnalyticsDTO;
import com.inkfront.logisticsApplication.service.interfaces.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard statistics endpoints")
public class DashboardController {

    private final DashboardService dashboardService;

    // ========== EXISTING ENDPOINTS (unchanged) ==========

    @GetMapping("/admin")
    @Operation(summary = "Get administrator dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getAdminDashboard() {
        log.info("Admin dashboard requested");
        DashboardStatsDTO response = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/super-admin")
    @Operation(summary = "Get super administrator dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getSuperAdminDashboard() {
        log.info("Super admin dashboard requested");
        DashboardStatsDTO response = dashboardService.getSuperAdminDashboardStats();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getDashboardSummary() {
        log.info("Dashboard summary requested");
        DashboardStatsDTO response = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/refresh")
    @Operation(summary = "Refresh dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> refreshDashboard() {
        log.info("Dashboard refresh requested");
        DashboardStatsDTO response = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponseDTO.success("Dashboard refreshed successfully", response));
    }

    // ========== NEW ENDPOINTS USING DTOs ==========

    @PostMapping("/summary/filtered")
    @Operation(summary = "Get dashboard summary with filters")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getDashboardSummaryFiltered(
            @Valid @RequestBody DashboardFilterRequestDTO request) {
        log.info("Filtered dashboard summary requested: {}", request);
        DashboardStatsDTO response = dashboardService.getDashboardSummary(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PostMapping("/revenue-analytics")
    @Operation(summary = "Get revenue analytics")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<RevenueAnalyticsDTO>> getRevenueAnalytics(
            @Valid @RequestBody RevenueAnalyticsRequestDTO request) {
        log.info("Revenue analytics requested from {} to {}", request.getStartDate(), request.getEndDate());
        RevenueAnalyticsDTO response = dashboardService.getRevenueAnalytics(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PostMapping("/driver-analytics")
    @Operation(summary = "Get driver analytics")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DriverAnalyticsDTO>> getDriverAnalytics(
            @Valid @RequestBody DriverAnalyticsRequestDTO request) {
        log.info("Driver analytics requested for driver: {}", request.getDriverId());
        DriverAnalyticsDTO response = dashboardService.getDriverAnalytics(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PostMapping("/order-analytics")
    @Operation(summary = "Get order analytics")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<OrderAnalyticsDTO>> getOrderAnalytics(
            @Valid @RequestBody OrderAnalyticsRequestDTO request) {
        log.info("Order analytics requested for status: {}", request.getStatus());
        OrderAnalyticsDTO response = dashboardService.getOrderAnalytics(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    // ========== NEW REVIEW ANALYTICS ENDPOINT ==========

    @GetMapping("/reviews")
    @Operation(summary = "Get review analytics for dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ReviewAnalyticsDTO>> getReviewAnalytics() {
        log.info("Review analytics requested for dashboard");
        ReviewAnalyticsDTO response = dashboardService.getReviewAnalytics();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }
}