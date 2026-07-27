package com.inkfront.logisticsApplication.controller.admin;

import com.inkfront.logisticsApplication.dto.response.admin.SystemConfigDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;

import com.inkfront.logisticsApplication.service.interfaces.*;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
public class SystemConfigController {


    private final SystemConfigService systemConfigService;


    @GetMapping("/system/configs")
    @Operation(summary = "Get system configurations")
    public ResponseEntity<ApiResponseDTO<List<SystemConfigDTO>>> getSystemConfigs() {
        log.info("Get system configs request");
        List<SystemConfigDTO> response = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @PutMapping("/system/configs")
    @Operation(summary = "Update system configuration")
    public ResponseEntity<ApiResponseDTO<SystemConfigDTO>> updateSystemConfig(
            @RequestParam String key,
            @RequestParam String value) {
        log.info("Update system config request for key: {}", key);
        SystemConfigDTO response = systemConfigService.updateConfig(key, value);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.CONFIG_UPDATED, response));
    }

}