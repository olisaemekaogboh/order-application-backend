package com.inkfront.logisticsApplication.controller.audit;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.audit.*;
import com.inkfront.logisticsApplication.dto.response.audit.*;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.AuditLogDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Management", description = "Audit log management endpoints")
public class AuditController {

    private final AuditService auditService;

    // ==================== NEW DTO‑BASED ENDPOINTS ====================

    @PostMapping("/search")
    @Operation(summary = "Search audit logs with filters (pagination, sorting)")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<AuditLogDTO>>> searchAudits(
            @Valid @RequestBody AuditSearchRequestDTO request) {

        log.info("Audit search request: userId={}, action={}, entityType={}, from {} to {}",
                request.getUserId(), request.getAction(), request.getEntityType(),
                request.getStartDate(), request.getEndDate());

        PaginatedResponseDTO<AuditLogDTO> response = auditService.searchAudits(request);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    @PostMapping("/user-activity")
    @Operation(summary = "Get user activity report")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<UserActivityDTO>> getUserActivity(
            @Valid @RequestBody UserActivityRequestDTO request) {

        log.info("User activity request for user: {}", request.getUserId());

        UserActivityDTO response = auditService.getUserActivity(request);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    @PostMapping("/export")
    @Operation(summary = "Export audit logs")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<ExportResponseDTO>> exportAudits(
            @Valid @RequestBody AuditExportRequestDTO request) {

        log.info("Audit export request: format={}, action={}, from {} to {}",
                request.getFormat(), request.getAction(), request.getStartDate(), request.getEndDate());

        ExportResponseDTO response = auditService.exportAudits(request);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Export initiated successfully", response)
        );
    }

    @PutMapping("/retention")
    @Operation(summary = "Update audit retention policy")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<AuditRetentionDTO>> updateRetentionPolicy(
            @Valid @RequestBody AuditRetentionRequestDTO request) {

        log.info("Update retention policy request: {} days", request.getRetentionDays());

        AuditRetentionDTO response = auditService.updateRetentionPolicy(request);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Retention policy updated", response)
        );
    }

    // ==================== SIMPLE RETRIEVAL / MAINTENANCE ENDPOINTS ====================

    @GetMapping("/{auditId}")
    @Operation(summary = "Get audit log by ID")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<AuditLogDTO>> getAuditLogById(
            @PathVariable String auditId) {

        log.info("Get audit log by ID: {}", auditId);

        AuditLogDTO response = auditService.getAuditLogById(auditId);

        return ResponseEntity.ok(
                ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response)
        );
    }

    @DeleteMapping("/{auditId}")
    @Operation(summary = "Delete audit log")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAuditLog(
            @PathVariable String auditId) {

        log.info("Delete audit log: {}", auditId);

        auditService.deleteAuditLog(auditId);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Audit log deleted successfully", null)
        );
    }

    @DeleteMapping("/cleanup")
    @Operation(summary = "Delete old audit logs (older than specified days)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> cleanupAuditLogs(
            @RequestParam int olderThanDays) {

        log.info("Cleanup audit logs older than {} days", olderThanDays);

        auditService.cleanupOldAuditLogs(olderThanDays);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Audit cleanup completed successfully", null)
        );
    }
}