package com.inkfront.logisticsApplication.controller.admin;

import com.inkfront.logisticsApplication.dto.request.admin.PricingConfigRequestDTO;

import com.inkfront.logisticsApplication.dto.response.admin.PricingConfigDTO;

import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;

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
public class PricingController {

    private final PricingService pricingService;

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

}