package com.inkfront.logisticsApplication.controller.dashboard;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.response.admin.DashboardStatsDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.service.interfaces.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @GetMapping("/admin")
    @Operation(summary = "Get administrator dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getAdminDashboard() {

        log.info("Admin dashboard requested");

        DashboardStatsDTO response =
                dashboardService.getAdminDashboardStats();

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }

    @GetMapping("/super-admin")
    @Operation(summary = "Get super administrator dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getSuperAdminDashboard() {

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

        log.info("Dashboard summary requested");

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

        log.info("Dashboard refresh requested");

        DashboardStatsDTO response =
                dashboardService.getAdminDashboardStats();

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Dashboard refreshed successfully",
                        response
                )
        );
    }
}