package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.response.common.AuditLogDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditService {

    PaginatedResponseDTO<AuditLogDTO> getAuditLogs(int page, int size);

    AuditLogDTO getAuditLogById(String auditId);

    PaginatedResponseDTO<AuditLogDTO> getAuditLogsByUser(
            String userId,
            int page,
            int size);

    PaginatedResponseDTO<AuditLogDTO> getAuditLogsByAction(
            String action,
            int page,
            int size);

    PaginatedResponseDTO<AuditLogDTO> getAuditLogsByEntity(
            String entityType,
            int page,
            int size);

    PaginatedResponseDTO<AuditLogDTO> getAuditLogsByDateRange(
            LocalDateTime start,
            LocalDateTime end,
            int page,
            int size);

    void deleteAuditLog(String auditId);

    void cleanupAuditLogs(int olderThanDays);
}