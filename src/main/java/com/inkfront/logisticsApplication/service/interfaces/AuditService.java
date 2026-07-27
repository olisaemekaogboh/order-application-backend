package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.audit.*;
import com.inkfront.logisticsApplication.dto.response.audit.*;
import com.inkfront.logisticsApplication.dto.response.common.AuditLogDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;

import java.time.LocalDateTime;

public interface AuditService {

    // ===== New DTO‑based methods =====

    PaginatedResponseDTO<AuditLogDTO> searchAudits(AuditSearchRequestDTO request);

    UserActivityDTO getUserActivity(UserActivityRequestDTO request);

    ExportResponseDTO exportAudits(AuditExportRequestDTO request);

    AuditRetentionDTO updateRetentionPolicy(AuditRetentionRequestDTO request);

    // ===== Legacy methods (kept for backward compatibility, but can be removed) =====

    @Deprecated
    void logAction(String userIdentifier, String action, String entityType, String entityId, String details);

    @Deprecated
    void logActionWithMetadata(String userIdentifier, String action, String entityType,
                               String entityId, String details, String ipAddress, String userAgent);

    @Deprecated
    AuditLogDTO getAuditLogById(String logId);

    @Deprecated
    PaginatedResponseDTO<AuditLogDTO> getAllAuditLogs(int page, int size, String sortBy, String sortDirection);

    @Deprecated
    PaginatedResponseDTO<AuditLogDTO> getUserAuditLogs(String userId, int page, int size);

    @Deprecated
    PaginatedResponseDTO<AuditLogDTO> getActionAuditLogs(String action, int page, int size);

    @Deprecated
    PaginatedResponseDTO<AuditLogDTO> getEntityAuditLogs(String entityType, String entityId, int page, int size);

    @Deprecated
    void deleteAuditLog(String logId);

    @Deprecated
    void cleanupOldAuditLogs(int daysToKeep);

    @Deprecated
    long countAuditLogsSince(LocalDateTime date);
}