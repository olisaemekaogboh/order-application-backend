package com.inkfront.logisticsApplication.controller.vehicle;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleMaintenanceRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleMaintenanceDTO;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import com.inkfront.logisticsApplication.service.interfaces.vehicle.VehicleMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/vehicles/{vehicleId}/maintenance")
@RequiredArgsConstructor
@Tag(name = "Vehicle Maintenance", description = "Vehicle maintenance endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
public class VehicleMaintenanceController {

    private final VehicleMaintenanceService maintenanceService;

    @PostMapping
    @Operation(summary = "Schedule maintenance for a vehicle")
    public ResponseEntity<ApiResponseDTO<VehicleMaintenanceDTO>> scheduleMaintenance(
            @PathVariable String vehicleId,
            @Valid @RequestBody VehicleMaintenanceRequestDTO request,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Schedule maintenance for vehicle {} by user: {}", vehicleId, user.getId());
        VehicleMaintenanceDTO response = maintenanceService.scheduleMaintenance(vehicleId, request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success("Maintenance scheduled", response));
    }

    @PutMapping("/{maintenanceId}")
    @Operation(summary = "Update maintenance record")
    public ResponseEntity<ApiResponseDTO<VehicleMaintenanceDTO>> updateMaintenance(
            @PathVariable String vehicleId,
            @PathVariable String maintenanceId,
            @Valid @RequestBody VehicleMaintenanceRequestDTO request,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Update maintenance {} for vehicle {} by user: {}", maintenanceId, vehicleId, user.getId());
        VehicleMaintenanceDTO response = maintenanceService.updateMaintenance(maintenanceId, request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.UPDATED_SUCCESSFULLY, response));
    }

    @PostMapping("/{maintenanceId}/complete")
    @Operation(summary = "Complete maintenance")
    public ResponseEntity<ApiResponseDTO<VehicleMaintenanceDTO>> completeMaintenance(
            @PathVariable String vehicleId,
            @PathVariable String maintenanceId,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Complete maintenance {} for vehicle {} by user: {}", maintenanceId, vehicleId, user.getId());
        VehicleMaintenanceDTO response = maintenanceService.completeMaintenance(maintenanceId, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success("Maintenance completed", response));
    }

    @GetMapping
    @Operation(summary = "Get maintenance history for vehicle")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<VehicleMaintenanceDTO>>> getMaintenanceHistory(
            @PathVariable String vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get maintenance history for vehicle: {}", vehicleId);
        PaginatedResponseDTO<VehicleMaintenanceDTO> response =
                maintenanceService.getMaintenanceHistory(vehicleId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }
}