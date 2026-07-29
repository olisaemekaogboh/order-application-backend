package com.inkfront.logisticsApplication.controller.dispatch;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.dispatch.*;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchResponseDTO;
import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchSummaryDTO;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import com.inkfront.logisticsApplication.service.interfaces.dispatch.DispatchService;
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
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
@Tag(name = "Dispatch Management", description = "Dispatch orchestration endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DispatchController {

    private final DispatchService dispatchService;

    @PostMapping
    @Operation(summary = "Create a new dispatch")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ResponseEntity<ApiResponseDTO<DispatchResponseDTO>> createDispatch(
            Authentication authentication,
            @Valid @RequestBody DispatchRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Create dispatch request for order: {} by user: {}", request.getOrderId(), user.getId());
        DispatchResponseDTO response = dispatchService.createDispatch(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_SAVED, response));
    }

    @PostMapping("/{dispatchId}/assign-driver")
    @Operation(summary = "Assign a driver to dispatch")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ResponseEntity<ApiResponseDTO<DispatchResponseDTO>> assignDriver(
            @PathVariable String dispatchId,
            @Valid @RequestBody AssignDriverRequestDTO request,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Assign driver {} to dispatch {} by user: {}", request.getDriverId(), dispatchId, user.getId());
        DispatchResponseDTO response = dispatchService.assignDriver(dispatchId, request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DRIVER_ASSIGNED, response));
    }

    @PostMapping("/{dispatchId}/assign-vehicle")
    @Operation(summary = "Assign a vehicle to dispatch")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ResponseEntity<ApiResponseDTO<DispatchResponseDTO>> assignVehicle(
            @PathVariable String dispatchId,
            @Valid @RequestBody AssignVehicleRequestDTO request,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Assign vehicle {} to dispatch {} by user: {}", request.getVehicleId(), dispatchId, user.getId());
        DispatchResponseDTO response = dispatchService.assignVehicle(dispatchId, request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.VEHICLE_ASSIGNED, response));
    }

    @PostMapping("/{dispatchId}/accept")
    @Operation(summary = "Accept a dispatch (driver)")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponseDTO<DispatchResponseDTO>> acceptDispatch(
            @PathVariable String dispatchId,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Accept dispatch {} by driver: {}", dispatchId, user.getId());
        DispatchResponseDTO response = dispatchService.acceptDispatch(dispatchId, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.OPERATION_SUCCESS, response));
    }

    @PostMapping("/{dispatchId}/reject")
    @Operation(summary = "Reject a dispatch (driver)")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponseDTO<DispatchResponseDTO>> rejectDispatch(
            @PathVariable String dispatchId,
            @RequestParam String reason,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Reject dispatch {} by driver: {} reason: {}", dispatchId, user.getId(), reason);
        DispatchResponseDTO response = dispatchService.rejectDispatch(dispatchId, reason, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.OPERATION_SUCCESS, response));
    }

    @PostMapping("/{dispatchId}/reassign")
    @Operation(summary = "Reassign a dispatch")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ResponseEntity<ApiResponseDTO<DispatchResponseDTO>> reassignDispatch(
            @PathVariable String dispatchId,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Reassign dispatch {} by user: {}", dispatchId, user.getId());
        DispatchResponseDTO response = dispatchService.reassignDispatch(dispatchId, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.OPERATION_SUCCESS, response));
    }

    @PostMapping("/{dispatchId}/cancel")
    @Operation(summary = "Cancel a dispatch")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ResponseEntity<ApiResponseDTO<DispatchResponseDTO>> cancelDispatch(
            @PathVariable String dispatchId,
            @RequestParam String reason,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Cancel dispatch {} by user: {} reason: {}", dispatchId, user.getId(), reason);
        DispatchResponseDTO response = dispatchService.cancelDispatch(dispatchId, reason, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.OPERATION_SUCCESS, response));
    }

    @PostMapping("/{dispatchId}/complete")
    @Operation(summary = "Complete a dispatch")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    public ResponseEntity<ApiResponseDTO<DispatchResponseDTO>> completeDispatch(
            @PathVariable String dispatchId,
            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Complete dispatch {} by user: {}", dispatchId, user.getId());
        DispatchResponseDTO response = dispatchService.completeDispatch(dispatchId, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.OPERATION_SUCCESS, response));
    }

    @GetMapping("/{dispatchId}")
    @Operation(summary = "Get dispatch by ID")
    public ResponseEntity<ApiResponseDTO<DispatchResponseDTO>> getDispatchById(@PathVariable String dispatchId) {
        log.info("Get dispatch by ID: {}", dispatchId);
        DispatchResponseDTO response = dispatchService.getDispatchById(dispatchId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get dispatch by order ID")
    public ResponseEntity<ApiResponseDTO<DispatchResponseDTO>> getDispatchByOrder(@PathVariable String orderId) {
        log.info("Get dispatch by order ID: {}", orderId);
        DispatchResponseDTO response = dispatchService.getDispatchByOrder(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get dispatches for a driver")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<DispatchSummaryDTO>>> getDispatchesByDriver(
            @PathVariable String driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get dispatches for driver: {}", driverId);
        PaginatedResponseDTO<DispatchSummaryDTO> response =
                dispatchService.getDispatchesByDriver(driverId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get dispatches for a vehicle")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<DispatchSummaryDTO>>> getDispatchesByVehicle(
            @PathVariable String vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get dispatches for vehicle: {}", vehicleId);
        PaginatedResponseDTO<DispatchSummaryDTO> response =
                dispatchService.getDispatchesByVehicle(vehicleId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping
    @Operation(summary = "Get all dispatches with filters")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<DispatchSummaryDTO>>> getAllDispatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Get all dispatches with filters");
        PaginatedResponseDTO<DispatchSummaryDTO> response =
                dispatchService.getAllDispatches(page, size, status, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }
}