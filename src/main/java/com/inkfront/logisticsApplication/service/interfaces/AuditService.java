package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.response.common.AuditLogDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditService {

    void logAction(String userId, String action, String entityType, String entityId, String details);

    void logActionWithMetadata(String userId, String action, String entityType, String entityId,
                               String details, String ipAddress, String userAgent);

    AuditLogDTO getAuditLogById(String logId);

    PaginatedResponseDTO<AuditLogDTO> getUserAuditLogs(String userId, int page, int size);

    PaginatedResponseDTO<AuditLogDTO> getEntityAuditLogs(String entityType, String entityId, int page, int size);

    PaginatedResponseDTO<AuditLogDTO> getAllAuditLogs(int page, int size, String sortBy, String sortDirection);

    List<AuditLogDTO> getAuditLogsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    void cleanupOldAuditLogs(int daysToKeep);

    long countAuditLogsSince(LocalDateTime date);
}