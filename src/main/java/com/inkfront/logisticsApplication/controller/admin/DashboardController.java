package com.inkfront.logisticsApplication.controller.admin;


import com.inkfront.logisticsApplication.dto.response.admin.DashboardStatsDTO;

import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;

import com.inkfront.logisticsApplication.service.interfaces.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Admin management endpoints")
public class DashboardController {

    private final DashboardService dashboardService;


    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard stats")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getDashboardStats() {
        log.info("Get admin dashboard stats request");
        DashboardStatsDTO response = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponseDTO.success("Dashboard stats retrieved", response));
    }

    @GetMapping("/dashboard/super")
    @Operation(summary = "Get super admin dashboard stats")
    public ResponseEntity<ApiResponseDTO<DashboardStatsDTO>> getSuperAdminDashboardStats() {
        log.info("Get super admin dashboard stats request");
        DashboardStatsDTO response = dashboardService.getSuperAdminDashboardStats();
        return ResponseEntity.ok(ApiResponseDTO.success("Super admin dashboard stats retrieved", response));
    }


}