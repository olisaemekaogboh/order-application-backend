package com.inkfront.logisticsApplication.controller.vehicle;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleFilterRequestDTO;
import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleSummaryDTO;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import com.inkfront.logisticsApplication.service.interfaces.vehicle.VehicleService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management", description = "Vehicle management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "Create a new vehicle")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public ResponseEntity<ApiResponseDTO<VehicleResponseDTO>> createVehicle(
            Authentication authentication,
            @Valid @RequestBody VehicleRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Create vehicle request by user: {}", user.getId());
        VehicleResponseDTO response = vehicleService.createVehicle(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.CREATED_SUCCESSFULLY, response));
    }

    @PutMapping("/{vehicleId}")
    @Operation(summary = "Update vehicle details")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public ResponseEntity<ApiResponseDTO<VehicleResponseDTO>> updateVehicle(
            @PathVariable String vehicleId,
            @Valid @RequestBody VehicleRequestDTO request,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Update vehicle request for: {} by user: {}", vehicleId, user.getId());
        VehicleResponseDTO response = vehicleService.updateVehicle(vehicleId, request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.UPDATED_SUCCESSFULLY, response));
    }

    @GetMapping("/{vehicleId}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<ApiResponseDTO<VehicleResponseDTO>> getVehicleById(@PathVariable String vehicleId) {
        log.info("Get vehicle by ID: {}", vehicleId);
        VehicleResponseDTO response = vehicleService.getVehicleById(vehicleId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/number/{vehicleNumber}")
    @Operation(summary = "Get vehicle by vehicle number")
    public ResponseEntity<ApiResponseDTO<VehicleResponseDTO>> getVehicleByNumber(@PathVariable String vehicleNumber) {
        log.info("Get vehicle by number: {}", vehicleNumber);
        VehicleResponseDTO response = vehicleService.getVehicleByNumber(vehicleNumber);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping
    @Operation(summary = "Search vehicles with filters")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<VehicleSummaryDTO>>> searchVehicles(
            @Valid VehicleFilterRequestDTO filter) {
        log.info("Search vehicles with filters: {}", filter);
        PaginatedResponseDTO<VehicleSummaryDTO> response = vehicleService.searchVehicles(filter);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get vehicles by status")
    public ResponseEntity<ApiResponseDTO<List<VehicleSummaryDTO>>> getVehiclesByStatus(@PathVariable String status) {
        log.info("Get vehicles by status: {}", status);
        List<VehicleSummaryDTO> response = vehicleService.getVehiclesByStatus(status);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @DeleteMapping("/{vehicleId}")
    @Operation(summary = "Delete vehicle (soft delete)")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteVehicle(
            @PathVariable String vehicleId,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Delete vehicle request for: {} by user: {}", vehicleId, user.getId());
        vehicleService.deleteVehicle(vehicleId, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DELETED_SUCCESSFULLY, null));
    }

    @PatchMapping("/{vehicleId}/status")
    @Operation(summary = "Update vehicle status")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public ResponseEntity<ApiResponseDTO<VehicleResponseDTO>> updateVehicleStatus(
            @PathVariable String vehicleId,
            @RequestParam String status,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Update vehicle status for: {} to {} by user: {}", vehicleId, status, user.getId());
        VehicleResponseDTO response = vehicleService.updateVehicleStatus(vehicleId, status, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.UPDATED_SUCCESSFULLY, response));
    }
}