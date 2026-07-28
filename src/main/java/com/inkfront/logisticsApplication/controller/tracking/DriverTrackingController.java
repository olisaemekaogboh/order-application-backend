package com.inkfront.logisticsApplication.controller.tracking;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.tracking.LocationUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.request.tracking.StatusUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.tracking.TrackingSessionResponseDTO;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import com.inkfront.logisticsApplication.service.interfaces.tracking.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/driver/tracking")
@RequiredArgsConstructor
@Tag(name = "Driver Tracking", description = "Driver tracking management endpoints")
public class DriverTrackingController {

    private final TrackingService trackingService;

    @PostMapping("/location")
    @Operation(summary = "Update location for assigned tracking")
    public ResponseEntity<ApiResponseDTO<TrackingSessionResponseDTO>> updateLocation(
            Authentication authentication,
            @Valid @RequestBody LocationUpdateRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Driver location update for tracking: {} by driver: {}", request.getTrackingId(), user.getId());
        TrackingSessionResponseDTO response = trackingService.updateLocation(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.TRACKING_UPDATED, response));
    }

    @PutMapping("/status")
    @Operation(summary = "Update tracking status (driver)")
    public ResponseEntity<ApiResponseDTO<TrackingSessionResponseDTO>> updateStatus(
            Authentication authentication,
            @Valid @RequestBody StatusUpdateRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Driver status update for tracking: {} by driver: {}", request.getTrackingId(), user.getId());
        TrackingSessionResponseDTO response = trackingService.updateStatus(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.TRACKING_UPDATED, response));
    }

    @GetMapping("/assigned")
    @Operation(summary = "Get assigned tracking sessions for current driver")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<TrackingSessionResponseDTO>>> getAssignedTracking(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Get assigned tracking for driver: {}", user.getId());
        PaginatedResponseDTO<TrackingSessionResponseDTO> response =
                trackingService.getTrackingByDriver(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }
}