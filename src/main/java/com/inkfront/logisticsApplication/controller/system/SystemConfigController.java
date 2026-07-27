package com.inkfront.logisticsApplication.controller.system;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.admin.SystemConfigRequestDTO;
import com.inkfront.logisticsApplication.dto.request.admin.SystemConfigUpdateRequestDTO;
import com.inkfront.logisticsApplication.dto.response.admin.SystemConfigDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.service.interfaces.SystemConfigService;
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
@RequestMapping("/api/system-config")
@RequiredArgsConstructor
@Tag(
        name = "System Configuration",
        description = "Manage application system configuration"
)
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @Operation(summary = "Get all configurations")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<SystemConfigDTO>>> getAllConfigs() {

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        systemConfigService.getAllConfigs()
                )
        );
    }

    @GetMapping("/{configId}")
    @Operation(summary = "Get configuration by ID")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<SystemConfigDTO>> getConfigById(
            @PathVariable String configId) {

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        systemConfigService.getConfigById(configId)
                )
        );
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "Get configuration by key")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<SystemConfigDTO>> getConfigByKey(
            @PathVariable String key) {

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        systemConfigService.getConfigByKey(key)
                )
        );
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get configurations by category")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<SystemConfigDTO>>> getConfigsByCategory(
            @PathVariable String category) {

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        systemConfigService.getConfigsByCategory(category)
                )
        );
    }

    @PostMapping
    @Operation(summary = "Create configuration")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<SystemConfigDTO>> createConfig(
            @RequestBody @Valid SystemConfigRequestDTO request) {

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.CREATED_SUCCESSFULLY,
                        systemConfigService.createConfig(request)
                )
        );
    }

    @PutMapping("/{key}")
    @Operation(summary = "Update configuration value")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<SystemConfigDTO>> updateConfig(
            @PathVariable String key,
            @RequestBody @Valid
            SystemConfigUpdateRequestDTO request) {

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.UPDATED_SUCCESSFULLY,
                        systemConfigService.updateConfig(key, request)
                )
        );
    }

    @DeleteMapping("/{configId}")
    @Operation(summary = "Delete configuration by ID")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteConfig(
            @PathVariable String configId) {

        systemConfigService.deleteConfig(configId);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DELETED_SUCCESSFULLY,
                        null
                )
        );
    }

    @DeleteMapping("/key/{key}")
    @Operation(summary = "Delete configuration by key")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteConfigByKey(
            @PathVariable String key) {

        systemConfigService.deleteConfigByKey(key);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DELETED_SUCCESSFULLY,
                        null
                )
        );
    }

    @PostMapping("/refresh-cache")
    @Operation(summary = "Refresh configuration cache")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> refreshCache() {

        systemConfigService.refreshCache();

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Configuration cache refreshed successfully",
                        null
                )
        );
    }

    @PostMapping("/load-cache")
    @Operation(summary = "Load all configurations into cache")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> loadCache() {

        systemConfigService.loadAllConfigsIntoCache();

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Configuration cache loaded successfully",
                        null
                )
        );
    }
}