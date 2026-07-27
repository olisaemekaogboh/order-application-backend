package com.inkfront.logisticsApplication.controller.admin;

import com.inkfront.logisticsApplication.dto.request.admin.SystemConfigUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.admin.SystemConfigDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.service.interfaces.SystemConfigService;
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
public class AdminSystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/system/configs")
    @Operation(summary = "Get system configurations")
    public ResponseEntity<ApiResponseDTO<List<SystemConfigDTO>>> getSystemConfigs() {
        log.info("Get system configs request");
        List<SystemConfigDTO> response = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PutMapping("/system/configs/{key}")
    @Operation(summary = "Update system configuration")
    public ResponseEntity<ApiResponseDTO<SystemConfigDTO>> updateSystemConfig(
            @PathVariable String key,
            @Valid @RequestBody SystemConfigUpdateRequestDTO request) {
        log.info("Update system config request for key: {}", key);
        SystemConfigDTO response = systemConfigService.updateConfig(key, request);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.CONFIG_UPDATED, response));
    }
}