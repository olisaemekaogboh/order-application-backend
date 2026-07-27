package com.inkfront.logisticsApplication.controller.admin;

import com.inkfront.logisticsApplication.dto.request.admin.PricingConfigRequestDTO;
import com.inkfront.logisticsApplication.dto.response.admin.DashboardStatsDTO;
import com.inkfront.logisticsApplication.dto.response.admin.PricingConfigDTO;
import com.inkfront.logisticsApplication.dto.response.admin.SystemConfigDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.AuditLogDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import com.inkfront.logisticsApplication.service.interfaces.*;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Admin management endpoints")
public class AdminController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final DriverService driverService;
    private final PricingService pricingService;
    private final AuditService auditService;
    private final SystemConfigService systemConfigService;

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

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Get all users request");
        PaginatedResponseDTO<UserDTO> response;
        if (role != null) {
            response = userService.getUsersByRole(role, page, size);
        } else {
            response = userService.getAllUsers(page, size, sortBy, sortDirection);
        }
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PutMapping("/users/{userId}/enable")
    @Operation(summary = "Enable user")
    public ResponseEntity<ApiResponseDTO<Void>> enableUser(@PathVariable String userId) {
        log.info("Enable user request for: {}", userId);
        userService.enableUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("User enabled successfully", null));
    }

    @PutMapping("/users/{userId}/disable")
    @Operation(summary = "Disable user")
    public ResponseEntity<ApiResponseDTO<Void>> disableUser(@PathVariable String userId) {
        log.info("Disable user request for: {}", userId);
        userService.disableUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("User disabled successfully", null));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable String userId) {
        log.info("Delete user request for: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.USER_DELETED, null));
    }

    @GetMapping("/pricing")
    @Operation(summary = "Get all pricing configurations")
    public ResponseEntity<ApiResponseDTO<List<PricingConfigDTO>>> getAllPricingConfigs() {
        log.info("Get all pricing configs request");
        List<PricingConfigDTO> response = pricingService.getAllPricingConfigs();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PostMapping("/pricing")
    @Operation(summary = "Create pricing configuration")
    public ResponseEntity<ApiResponseDTO<PricingConfigDTO>> createPricingConfig(
            @Valid @RequestBody PricingConfigRequestDTO request) {
        log.info("Create pricing config request for vehicle: {}", request.getVehicleType());
        PricingConfigDTO response = pricingService.createPricingConfig(request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.PRICING_UPDATED, response));
    }

    @PutMapping("/pricing/{configId}")
    @Operation(summary = "Update pricing configuration")
    public ResponseEntity<ApiResponseDTO<PricingConfigDTO>> updatePricingConfig(
            @PathVariable String configId,
            @Valid @RequestBody PricingConfigRequestDTO request) {
        log.info("Update pricing config request for: {}", configId);
        PricingConfigDTO response = pricingService.updatePricingConfig(configId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.PRICING_UPDATED, response));
    }

    @PutMapping("/pricing/{configId}/activate")
    @Operation(summary = "Activate pricing configuration")
    public ResponseEntity<ApiResponseDTO<Void>> activatePricingConfig(@PathVariable String configId) {
        log.info("Activate pricing config request for: {}", configId);
        pricingService.activatePricingConfig(configId);
        return ResponseEntity.ok(ApiResponseDTO.success("Pricing configuration activated", null));
    }

    @PutMapping("/pricing/{configId}/deactivate")
    @Operation(summary = "Deactivate pricing configuration")
    public ResponseEntity<ApiResponseDTO<Void>> deactivatePricingConfig(@PathVariable String configId) {
        log.info("Deactivate pricing config request for: {}", configId);
        pricingService.deactivatePricingConfig(configId);
        return ResponseEntity.ok(ApiResponseDTO.success("Pricing configuration deactivated", null));
    }

    @DeleteMapping("/pricing/{configId}")
    @Operation(summary = "Delete pricing configuration")
    public ResponseEntity<ApiResponseDTO<Void>> deletePricingConfig(@PathVariable String configId) {
        log.info("Delete pricing config request for: {}", configId);
        pricingService.deletePricingConfig(configId);
        return ResponseEntity.ok(ApiResponseDTO.success("Pricing configuration deleted", null));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<AuditLogDTO>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Get audit logs request");
        PaginatedResponseDTO<AuditLogDTO> response = auditService.getAllAuditLogs(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/audit-logs/user/{userId}")
    @Operation(summary = "Get audit logs for user")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<AuditLogDTO>>> getUserAuditLogs(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get user audit logs request for: {}", userId);
        PaginatedResponseDTO<AuditLogDTO> response = auditService.getUserAuditLogs(userId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/system/configs")
    @Operation(summary = "Get system configurations")
    public ResponseEntity<ApiResponseDTO<List<SystemConfigDTO>>> getSystemConfigs() {
        log.info("Get system configs request");
        List<SystemConfigDTO> response = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PutMapping("/system/configs")
    @Operation(summary = "Update system configuration")
    public ResponseEntity<ApiResponseDTO<SystemConfigDTO>> updateSystemConfig(
            @RequestParam String key,
            @RequestParam String value) {
        log.info("Update system config request for key: {}", key);
        SystemConfigDTO response = systemConfigService.updateConfig(key, value);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.CONFIG_UPDATED, response));
    }
    @PostMapping("/drivers/{driverId}/payments")
    public ResponseEntity<ApiResponseDTO<Void>> processDriverPayment(
            @PathVariable String driverId,
            @RequestParam Double amount) {

        log.info("Processing payment for driver {}", driverId);

        driverService.processDriverPayment(driverId, amount);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Driver payment processed successfully",
                        null
                )
        );
    }
}