package com.inkfront.logisticsApplication.controller.admin;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.tracking.TrackingSessionResponseDTO;
import com.inkfront.logisticsApplication.service.interfaces.tracking.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/tracking")
@RequiredArgsConstructor
@Tag(name = "Admin Tracking", description = "Admin monitoring endpoints")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminTrackingController {

    private final TrackingService trackingService;

    @GetMapping
    @Operation(summary = "Get all tracking sessions")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<TrackingSessionResponseDTO>>> getAllTracking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Admin get all tracking with filters");
        PaginatedResponseDTO<TrackingSessionResponseDTO> response =
                trackingService.getAllTracking(page, size, status, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }
}