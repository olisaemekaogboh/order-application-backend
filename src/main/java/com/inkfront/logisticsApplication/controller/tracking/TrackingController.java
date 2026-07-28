package com.inkfront.logisticsApplication.controller.tracking;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.tracking.*;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.tracking.*;
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
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Tag(name = "Tracking", description = "Shipment and order tracking endpoints")
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/start")
    @Operation(summary = "Start tracking for an order")
    public ResponseEntity<ApiResponseDTO<TrackingSessionResponseDTO>> startTracking(
            Authentication authentication,
            @Valid @RequestBody StartTrackingRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Start tracking request for order: {} by user: {}", request.getOrderId(), user.getId());
        TrackingSessionResponseDTO response = trackingService.startTracking(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.TRACKING_STARTED, response));
    }

    @PostMapping("/location")
    @Operation(summary = "Update location for a tracking session")
    public ResponseEntity<ApiResponseDTO<TrackingSessionResponseDTO>> updateLocation(
            Authentication authentication,
            @Valid @RequestBody LocationUpdateRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Location update for tracking: {} by user: {}", request.getTrackingId(), user.getId());
        TrackingSessionResponseDTO response = trackingService.updateLocation(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.TRACKING_UPDATED, response));
    }

    @PutMapping("/status")
    @Operation(summary = "Update tracking status")
    public ResponseEntity<ApiResponseDTO<TrackingSessionResponseDTO>> updateStatus(
            Authentication authentication,
            @Valid @RequestBody StatusUpdateRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Status update for tracking: {} by user: {}", request.getTrackingId(), user.getId());
        TrackingSessionResponseDTO response = trackingService.updateStatus(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.TRACKING_UPDATED, response));
    }

    @PostMapping("/complete")
    @Operation(summary = "Complete tracking")
    public ResponseEntity<ApiResponseDTO<TrackingSessionResponseDTO>> completeTracking(
            Authentication authentication,
            @Valid @RequestBody CompleteTrackingRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Complete tracking request for: {} by user: {}", request.getTrackingId(), user.getId());
        TrackingSessionResponseDTO response = trackingService.completeTracking(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.TRACKING_COMPLETED, response));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel tracking")
    public ResponseEntity<ApiResponseDTO<TrackingSessionResponseDTO>> cancelTracking(
            Authentication authentication,
            @Valid @RequestBody CancelTrackingRequestDTO request) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Cancel tracking request for: {} by user: {}", request.getTrackingId(), user.getId());
        TrackingSessionResponseDTO response = trackingService.cancelTracking(request, user.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.TRACKING_CANCELLED, response));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Get tracking session by ID")
    public ResponseEntity<ApiResponseDTO<TrackingSessionResponseDTO>> getTrackingById(
            @PathVariable String trackingId) {
        log.info("Get tracking by ID: {}", trackingId);
        TrackingSessionResponseDTO response = trackingService.getTrackingById(trackingId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/live/{trackingId}")
    @Operation(summary = "Get live tracking data")
    public ResponseEntity<ApiResponseDTO<LiveTrackingDTO>> getLiveTracking(
            @PathVariable String trackingId) {
        log.info("Get live tracking: {}", trackingId);
        LiveTrackingDTO response = trackingService.getLiveTracking(trackingId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/timeline/{trackingId}")
    @Operation(summary = "Get tracking timeline")
    public ResponseEntity<ApiResponseDTO<TrackingTimelineDTO>> getTimeline(
            @PathVariable String trackingId) {
        log.info("Get timeline for tracking: {}", trackingId);
        TrackingTimelineDTO response = trackingService.getTimeline(trackingId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/user")
    @Operation(summary = "Get tracking sessions for current user")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<TrackingSessionResponseDTO>>> getUserTracking(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        log.info("Get user tracking for user: {}", user.getId());
        PaginatedResponseDTO<TrackingSessionResponseDTO> response =
                trackingService.getTrackingByUser(user.getId(), page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }
}