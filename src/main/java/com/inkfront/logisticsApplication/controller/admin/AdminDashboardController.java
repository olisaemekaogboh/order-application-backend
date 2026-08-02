package com.inkfront.logisticsApplication.controller.admin;

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
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Admin dashboard endpoints")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    // ==========================================================
    // Dashboard
    // ==========================================================

    @GetMapping
    @Operation(summary = "Get admin dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getDashboardStats() {

        log.info("Admin dashboard requested");

        DashboardStatsDTO response = dashboardService.getAdminDashboardStats();

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }

    @GetMapping("/super")
    @Operation(summary = "Get super admin dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getSuperAdminDashboardStats() {

        log.info("Super admin dashboard requested");

        DashboardStatsDTO response =
                dashboardService.getSuperAdminDashboardStats();

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getDashboardSummary() {

        DashboardStatsDTO response =
                dashboardService.getAdminDashboardStats();

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }

    @GetMapping("/refresh")
    @Operation(summary = "Refresh dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> refreshDashboard() {

        DashboardStatsDTO response =
                dashboardService.getAdminDashboardStats();

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Dashboard refreshed successfully",
                        response
                )
        );
    }

    // ==========================================================
    // Filtered Summary
    // ==========================================================

    @PostMapping("/summary/filtered")
    @Operation(summary = "Dashboard summary with filters")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getDashboardSummaryFiltered(
            @Valid @RequestBody DashboardFilterRequestDTO request) {

        DashboardStatsDTO response =
                dashboardService.getDashboardSummary(request);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }

    // ==========================================================
    // Revenue Analytics
    // ==========================================================

    @PostMapping("/revenue-analytics")
    @Operation(summary = "Revenue analytics")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<RevenueAnalyticsDTO>> getRevenueAnalytics(
            @Valid @RequestBody RevenueAnalyticsRequestDTO request) {

        RevenueAnalyticsDTO response =
                dashboardService.getRevenueAnalytics(request);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }

    // ==========================================================
    // Order Analytics
    // ==========================================================

    @PostMapping("/order-analytics")
    @Operation(summary = "Order analytics")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<OrderAnalyticsDTO>> getOrderAnalytics(
            @Valid @RequestBody OrderAnalyticsRequestDTO request) {

        OrderAnalyticsDTO response =
                dashboardService.getOrderAnalytics(request);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }

    // ==========================================================
    // Driver Analytics
    // ==========================================================

    @PostMapping("/driver-analytics")
    @Operation(summary = "Driver analytics")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DriverAnalyticsDTO>> getDriverAnalytics(
            @Valid @RequestBody DriverAnalyticsRequestDTO request) {

        DriverAnalyticsDTO response =
                dashboardService.getDriverAnalytics(request);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }

    // ==========================================================
    // Review Analytics
    // ==========================================================

    @GetMapping("/reviews")
    @Operation(summary = "Review analytics")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ReviewAnalyticsDTO>> getReviewAnalytics() {

        ReviewAnalyticsDTO response =
                dashboardService.getReviewAnalytics();

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }
}