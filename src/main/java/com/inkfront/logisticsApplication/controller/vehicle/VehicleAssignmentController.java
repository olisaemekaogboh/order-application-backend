package com.inkfront.logisticsApplication.controller.vehicle;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.vehicle.VehicleAssignmentRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleAssignmentDTO;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import com.inkfront.logisticsApplication.service.interfaces.vehicle.VehicleAssignmentService;
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
@RequestMapping("/api/vehicles/{vehicleId}/assignment")
@RequiredArgsConstructor
@Tag(name = "Vehicle Assignment", description = "Vehicle assignment endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
public class VehicleAssignmentController {

    private final VehicleAssignmentService assignmentService;

    @PostMapping
    @Operation(summary = "Assign a driver to a vehicle")
    public ResponseEntity<ApiResponseDTO<VehicleAssignmentDTO>> assignDriver(
            @PathVariable String vehicleId,
            @Valid @RequestBody VehicleAssignmentRequestDTO request,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Assign driver {} to vehicle {} by user: {}", request.getDriverId(), vehicleId, user.getId());
        VehicleAssignmentDTO response = assignmentService.assignDriver(vehicleId, request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DRIVER_ASSIGNED, response));
    }

    @DeleteMapping
    @Operation(summary = "Release driver from vehicle")
    public ResponseEntity<ApiResponseDTO<VehicleAssignmentDTO>> releaseDriver(
            @PathVariable String vehicleId,
            @RequestParam String reason,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Release driver from vehicle {} by user: {}", vehicleId, user.getId());
        VehicleAssignmentDTO response = assignmentService.releaseDriver(vehicleId, reason, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success("Driver released successfully", response));
    }

    @GetMapping("/current")
    @Operation(summary = "Get current assignment for vehicle")
    public ResponseEntity<ApiResponseDTO<VehicleAssignmentDTO>> getCurrentAssignment(@PathVariable String vehicleId) {
        log.info("Get current assignment for vehicle: {}", vehicleId);
        VehicleAssignmentDTO response = assignmentService.getCurrentAssignment(vehicleId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get assignment history for vehicle")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<VehicleAssignmentDTO>>> getAssignmentHistory(
            @PathVariable String vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get assignment history for vehicle: {}", vehicleId);
        PaginatedResponseDTO<VehicleAssignmentDTO> response =
                assignmentService.getAssignmentsByVehicle(vehicleId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }
}