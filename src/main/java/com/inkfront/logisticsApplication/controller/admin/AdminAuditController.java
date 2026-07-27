package com.inkfront.logisticsApplication.controller.admin;

import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.AuditLogDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.service.interfaces.*;
import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Admin management endpoints")
public class AdminAuditController {


    private final AuditService auditService;



    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<AuditLogDTO>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Get audit logs request");
        PaginatedResponseDTO<AuditLogDTO> response = auditService.getAllAuditLogs(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }

    @GetMapping("/audit-logs/user/{userId}")
    @Operation(summary = "Get audit logs for user")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<AuditLogDTO>>> getUserAuditLogs(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get user audit logs request for: {}", userId);
        PaginatedResponseDTO<AuditLogDTO> response = auditService.getUserAuditLogs(userId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessMessages.DATA_RETRIEVED, response));
    }


}