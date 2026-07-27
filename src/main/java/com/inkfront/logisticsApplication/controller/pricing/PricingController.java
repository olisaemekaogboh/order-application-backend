package com.inkfront.logisticsApplication.controller.pricing;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import com.inkfront.logisticsApplication.dto.request.admin.PricingConfigRequestDTO;
import com.inkfront.logisticsApplication.dto.response.admin.PricingConfigDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.service.interfaces.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
@Tag(name = "Pricing Management", description = "Pricing configuration endpoints")
public class PricingController {

    private final PricingService pricingService;

    @GetMapping
    @Operation(summary = "Get all pricing configurations")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<PricingConfigDTO>>> getAllPricingConfigs() {

        log.info("Get all pricing configurations");

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        pricingService.getAllPricingConfigs()
                )
        );
    }

    @GetMapping("/active")
    @Operation(summary = "Get active pricing configurations")
    public ResponseEntity<ApiResponseDTO<List<PricingConfigDTO>>> getActivePricingConfigs() {

        log.info("Get active pricing configurations");

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        pricingService.getActivePricingConfigs()
                )
        );
    }

    @GetMapping("/{configId}")
    @Operation(summary = "Get pricing configuration by ID")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PricingConfigDTO>> getPricingConfig(
            @PathVariable String configId) {

        log.info("Get pricing configuration {}", configId);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        pricingService.getPricingConfigById(configId)
                )
        );
    }

    @GetMapping("/vehicle/{vehicleType}")
    @Operation(summary = "Get active pricing by vehicle type")
    public ResponseEntity<ApiResponseDTO<PricingConfigDTO>> getPricingByVehicleType(
            @PathVariable VehicleType vehicleType) {

        log.info("Get pricing for {}", vehicleType);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        pricingService.getActivePricingConfig(vehicleType)
                )
        );
    }

    @PostMapping
    @Operation(summary = "Create pricing configuration")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PricingConfigDTO>> createPricingConfig(
            @Valid @RequestBody PricingConfigRequestDTO request) {

        log.info("Create pricing configuration {}", request.getVehicleType());

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.PRICING_UPDATED,
                        pricingService.createPricingConfig(request)
                )
        );
    }

    @PutMapping("/{configId}")
    @Operation(summary = "Update pricing configuration")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PricingConfigDTO>> updatePricingConfig(
            @PathVariable String configId,
            @Valid @RequestBody PricingConfigRequestDTO request) {

        log.info("Update pricing configuration {}", configId);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.PRICING_UPDATED,
                        pricingService.updatePricingConfig(configId, request)
                )
        );
    }

    @PutMapping("/{configId}/activate")
    @Operation(summary = "Activate pricing configuration")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> activatePricingConfig(
            @PathVariable String configId) {

        pricingService.activatePricingConfig(configId);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Pricing configuration activated successfully",
                        null
                )
        );
    }

    @PutMapping("/{configId}/deactivate")
    @Operation(summary = "Deactivate pricing configuration")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deactivatePricingConfig(
            @PathVariable String configId) {

        pricingService.deactivatePricingConfig(configId);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Pricing configuration deactivated successfully",
                        null
                )
        );
    }

    @DeleteMapping("/{configId}")
    @Operation(summary = "Delete pricing configuration")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deletePricingConfig(
            @PathVariable String configId) {

        pricingService.deletePricingConfig(configId);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.PRICING_UPDATED,
                        null
                )
        );
    }
}