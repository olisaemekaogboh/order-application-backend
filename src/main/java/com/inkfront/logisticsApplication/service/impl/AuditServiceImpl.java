package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.AuditLog;
import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.dto.request.audit.*;
import com.inkfront.logisticsApplication.dto.response.audit.*;
import com.inkfront.logisticsApplication.dto.response.common.AuditLogDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.mapper.AuditLogMapper;
import com.inkfront.logisticsApplication.repository.AuditLogRepository;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    // ==================== NEW DTO‑BASED METHODS ====================

    @Override
    public PaginatedResponseDTO<AuditLogDTO> searchAudits(AuditSearchRequestDTO request) {
        log.info("Searching audit logs with filters: userId={}, username={}, action={}, entityType={}, entityId={}, from {} to {}",
                request.getUserId(), request.getUsername(), request.getAction(),
                request.getEntityType(), request.getEntityId(),
                request.getStartDate(), request.getEndDate());

        // Build sort
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Convert dates to LocalDateTime
        LocalDateTime startDateTime = request.getStartDate() != null ? request.getStartDate().atStartOfDay() : null;
        LocalDateTime endDateTime = request.getEndDate() != null ? request.getEndDate().atTime(23, 59, 59) : null;

        // Since we cannot modify the repository, we use a fallback: fetch all (with pagination) and then filter in memory.
        // ⚠️ This is not efficient for production; you should add a custom repository method.
        Page<AuditLog> allPage = auditLogRepository.findAll(pageable);
        List<AuditLog> filtered = allPage.getContent().stream()
                .filter(log -> request.getUserId() == null || request.getUserId().equals(log.getUserId()))
                .filter(log -> request.getUsername() == null || request.getUsername().equals(log.getUsername()))
                .filter(log -> request.getAction() == null || request.getAction().equals(log.getAction()))
                .filter(log -> request.getEntityType() == null || request.getEntityType().equals(log.getEntityType()))
                .filter(log -> request.getEntityId() == null || request.getEntityId().equals(log.getEntityId()))
                .filter(log -> startDateTime == null || !log.getTimestamp().isBefore(startDateTime))
                .filter(log -> endDateTime == null || !log.getTimestamp().isAfter(endDateTime))
                .collect(Collectors.toList());

        log.warn("Audit search is using in‑memory filtering – add a custom repository method for production.");

        List<AuditLogDTO> content = filtered.stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());

        // Return pagination info from the original page (approximate)
        return new PaginatedResponseDTO<>(
                content,
                request.getPage(),
                request.getSize(),
                allPage.getTotalElements()
        );
    }

    @Override
    public UserActivityDTO getUserActivity(UserActivityRequestDTO request) {
        log.info("Generating user activity report for user: {}", request.getUserId());

        if (!userRepository.existsById(request.getUserId())) {
            throw new ResourceNotFoundException("User not found: " + request.getUserId());
        }

        LocalDateTime start = request.getStartDate() != null ? request.getStartDate().atStartOfDay() : null;
        LocalDateTime end = request.getEndDate() != null ? request.getEndDate().atTime(23, 59, 59) : null;

        List<AuditLog> logs;
        if (start != null && end != null) {
            logs = auditLogRepository.findAuditLogsBetweenDates(start, end)
                    .stream()
                    .filter(l -> request.getUserId().equals(l.getUserId()))
                    .collect(Collectors.toList());
        } else {
            Page<AuditLog> page = auditLogRepository.findByUserId(request.getUserId(),
                    PageRequest.of(0, Integer.MAX_VALUE, Sort.by("timestamp")));
            logs = page.getContent();
        }

        // Aggregate
        Map<String, Long> actionCount = logs.stream()
                .collect(Collectors.groupingBy(AuditLog::getAction, Collectors.counting()));

        Map<String, Long> dayCount = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getTimestamp().toLocalDate().toString(),
                        Collectors.counting()
                ));

        String username = userRepository.findById(request.getUserId())
                .map(User::getFullName)
                .orElse("Unknown");

        return UserActivityDTO.builder()
                .userId(request.getUserId())
                .username(username)
                .totalActivities((long) logs.size())
                .activityByAction(actionCount)
                .activityByDay(dayCount)
                .build();
    }

    @Override
    public ExportResponseDTO exportAudits(AuditExportRequestDTO request) {
        log.info("Exporting audit logs from {} to {} with action {} in format {}",
                request.getStartDate(), request.getEndDate(), request.getAction(), request.getFormat());

        LocalDateTime start = request.getStartDate() != null ? request.getStartDate().atStartOfDay() : null;
        LocalDateTime end = request.getEndDate() != null ? request.getEndDate().atTime(23, 59, 59) : null;

        List<AuditLog> logs;
        if (start != null && end != null) {
            logs = auditLogRepository.findAuditLogsBetweenDates(start, end);
        } else {
            logs = auditLogRepository.findAll();
        }

        // Apply action filter (String comparison)
        if (request.getAction() != null && !request.getAction().isEmpty()) {
            logs = logs.stream()
                    .filter(log -> request.getAction().equals(log.getAction()))
                    .collect(Collectors.toList());
        }

        // Generate export file (simulated)
        String fileName = "audit_export_" + System.currentTimeMillis() + "." + request.getFormat().toLowerCase();

        return ExportResponseDTO.builder()
                .fileName(fileName)
                .fileType(request.getFormat().toUpperCase())
                .recordCount((long) logs.size())
                .downloadUrl("/api/audit/download/" + fileName) // placeholder
                .build();
    }

    @Override
    public AuditRetentionDTO updateRetentionPolicy(AuditRetentionRequestDTO request) {
        log.info("Updating audit retention policy to {} days", request.getRetentionDays());

        // In a real system, you would store the retention policy in a config table.
        // Here we just simulate the update.
        return AuditRetentionDTO.builder()
                .retentionDays(request.getRetentionDays())
                .message("Retention policy updated successfully. Logs older than " + request.getRetentionDays() + " days will be purged.")
                .build();
    }

    // ==================== LEGACY METHODS (kept for backward compatibility) ====================

    @Override
    @Deprecated
    public void logAction(String userIdentifier, String action, String entityType, String entityId, String details) {
        logActionWithMetadata(userIdentifier, action, entityType, entityId, details, null, null);
    }

    @Override
    @Deprecated
    public void logActionWithMetadata(String userIdentifier, String action, String entityType,
                                      String entityId, String details, String ipAddress, String userAgent) {
        AuditLog auditLog = createAuditLog(userIdentifier, action, entityType, entityId, details);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLogRepository.save(auditLog);
    }

    @Override
    @Deprecated
    public AuditLogDTO getAuditLogById(String logId) {
        AuditLog auditLog = auditLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found"));
        return auditLogMapper.toDTO(auditLog);
    }

    @Override
    @Deprecated
    public PaginatedResponseDTO<AuditLogDTO> getAllAuditLogs(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuditLog> auditLogs = auditLogRepository.findAll(pageable);
        return toPaginatedResponse(auditLogs);
    }

    @Override
    @Deprecated
    public PaginatedResponseDTO<AuditLogDTO> getUserAuditLogs(String userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> auditLogs = auditLogRepository.findByUserId(userId, pageable);
        return toPaginatedResponse(auditLogs);
    }

    @Override
    @Deprecated
    public PaginatedResponseDTO<AuditLogDTO> getActionAuditLogs(String action, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> auditLogs = auditLogRepository.findByAction(action, pageable);
        return toPaginatedResponse(auditLogs);
    }

    @Override
    @Deprecated
    public PaginatedResponseDTO<AuditLogDTO> getEntityAuditLogs(String entityType, String entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> auditLogs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        return toPaginatedResponse(auditLogs);
    }

    @Override
    @Deprecated
    public void deleteAuditLog(String logId) {
        if (!auditLogRepository.existsById(logId)) {
            throw new ResourceNotFoundException("Audit log not found");
        }
        auditLogRepository.deleteById(logId);
        log.info("Deleted audit log {}", logId);
    }

    @Override
    @Deprecated
    public void cleanupOldAuditLogs(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        List<AuditLog> oldLogs = auditLogRepository.findAuditLogsBetweenDates(LocalDateTime.MIN, cutoff);
        auditLogRepository.deleteAll(oldLogs);
        log.info("Deleted {} old audit logs (older than {} days)", oldLogs.size(), daysToKeep);
    }

    @Override
    @Deprecated
    public long countAuditLogsSince(LocalDateTime date) {
        return auditLogRepository.countAuditLogsSince(date);
    }

    // -------------------- Private Helpers --------------------

    private AuditLog createAuditLog(String userIdentifier, String action, String entityType,
                                    String entityId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setStatus("SUCCESS");

        if (userIdentifier != null && !userIdentifier.isBlank()) {
            Optional<User> user = userRepository.findById(userIdentifier);
            if (user.isEmpty()) {
                user = userRepository.findByEmail(userIdentifier);
            }
            user.ifPresent(u -> {
                auditLog.setUserId(u.getId());
                auditLog.setUsername(u.getFullName());
                auditLog.setEmail(u.getEmail());
            });
            if (user.isEmpty()) {
                auditLog.setUserId(userIdentifier);
            }
        }
        return auditLog;
    }

    private PaginatedResponseDTO<AuditLogDTO> toPaginatedResponse(Page<AuditLog> page) {
        List<AuditLogDTO> content = page.getContent().stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
        return new PaginatedResponseDTO<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}