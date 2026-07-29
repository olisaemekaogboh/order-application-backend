package com.inkfront.logisticsApplication.controller.vehicle;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.vehicle.VehicleAnalyticsDTO;
import com.inkfront.logisticsApplication.service.interfaces.vehicle.VehicleAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/vehicle-analytics")
@RequiredArgsConstructor
@Tag(name = "Vehicle Analytics", description = "Vehicle analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
public class VehicleAnalyticsController {

    private final VehicleAnalyticsService analyticsService;

    @GetMapping
    @Operation(summary = "Get vehicle analytics")
    public ResponseEntity<ApiResponseDTO<VehicleAnalyticsDTO>> getAnalytics() {
        log.info("Get vehicle analytics");
        VehicleAnalyticsDTO response = analyticsService.getAnalytics();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }
}