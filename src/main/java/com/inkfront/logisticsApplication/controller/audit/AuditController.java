package com.inkfront.logisticsApplication.controller.audit;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.response.audit.AuditLogDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Management", description = "Audit log management endpoints")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Get all audit logs")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<AuditLogDTO>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching audit logs");

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        auditService.getAuditLogs(page, size)
                )
        );
    }

    @GetMapping("/{auditId}")
    @Operation(summary = "Get audit log by ID")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<AuditLogDTO>> getAuditLogById(
            @PathVariable String auditId) {

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        auditService.getAuditLogById(auditId)
                )
        );
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs by user")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<AuditLogDTO>>> getAuditLogsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        auditService.getAuditLogsByUser(userId, page, size)
                )
        );
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Get audit logs by action")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<AuditLogDTO>>> getAuditLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        auditService.getAuditLogsByAction(action, page, size)
                )
        );
    }

    @GetMapping("/entity/{entityType}")
    @Operation(summary = "Get audit logs by entity")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<AuditLogDTO>>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        auditService.getAuditLogsByEntity(entityType, page, size)
                )
        );
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get audit logs within date range")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<AuditLogDTO>>> getAuditLogsByDateRange(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        auditService.getAuditLogsByDateRange(
                                start,
                                end,
                                page,
                                size
                        )
                )
        );
    }

    @DeleteMapping("/{auditId}")
    @Operation(summary = "Delete audit log")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAuditLog(
            @PathVariable String auditId) {

        auditService.deleteAuditLog(auditId);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Audit log deleted successfully",
                        null
                )
        );
    }

    @DeleteMapping("/cleanup")
    @Operation(summary = "Delete old audit logs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> cleanupAuditLogs(
            @RequestParam int olderThanDays) {

        auditService.cleanupAuditLogs(olderThanDays);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Audit cleanup completed",
                        null
                )
        );
    }

}