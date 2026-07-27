package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.domain.entity.AuditLog;
import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.dto.response.common.AuditLogDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    public void logAction(String userIdentifier,
                          String action,
                          String entityType,
                          String entityId,
                          String details) {

        auditLogRepository.save(
                createAuditLog(userIdentifier, action, entityType, entityId, details)
        );
    }

    @Override
    public void logActionWithMetadata(String userIdentifier,
                                      String action,
                                      String entityType,
                                      String entityId,
                                      String details,
                                      String ipAddress,
                                      String userAgent) {

        AuditLog auditLog = createAuditLog(
                userIdentifier,
                action,
                entityType,
                entityId,
                details
        );

        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);

        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogDTO getAuditLogById(String logId) {

        AuditLog auditLog = auditLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Audit log not found"));

        return auditLogMapper.toDTO(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<AuditLogDTO> getUserAuditLogs(String userId,
                                                              int page,
                                                              int size) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "timestamp")
        );

        Page<AuditLog> auditLogs =
                auditLogRepository.findByUserId(userId, pageable);

        List<AuditLogDTO> content = auditLogs.getContent()
                .stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(
                content,
                auditLogs.getNumber(),
                auditLogs.getSize(),
                auditLogs.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<AuditLogDTO> getEntityAuditLogs(String entityType,
                                                                String entityId,
                                                                int page,
                                                                int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "timestamp")
        );

        Page<AuditLog> auditLogs =
                auditLogRepository.findByEntityTypeAndEntityId(
                        entityType,
                        entityId,
                        pageable
                );

        List<AuditLogDTO> content = auditLogs.getContent()
                .stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(
                content,
                auditLogs.getNumber(),
                auditLogs.getSize(),
                auditLogs.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<AuditLogDTO> getAllAuditLogs(int page,
                                                             int size,
                                                             String sortBy,
                                                             String sortDirection) {

        Sort sort = Sort.by(
                Sort.Direction.fromString(sortDirection),
                sortBy
        );

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditLog> auditLogs = auditLogRepository.findAll(pageable);

        List<AuditLogDTO> content = auditLogs.getContent()
                .stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponseDTO<>(
                content,
                auditLogs.getNumber(),
                auditLogs.getSize(),
                auditLogs.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogsBetweenDates(LocalDateTime startDate,
                                                      LocalDateTime endDate) {

        return auditLogRepository
                .findAuditLogsBetweenDates(startDate, endDate)
                .stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void cleanupOldAuditLogs(int daysToKeep) {

        LocalDateTime cutoffDate =
                LocalDateTime.now().minusDays(daysToKeep);

        List<AuditLog> oldLogs =
                auditLogRepository.findAuditLogsBetweenDates(
                        LocalDateTime.MIN,
                        cutoffDate
                );

        auditLogRepository.deleteAll(oldLogs);

        log.info("Deleted {} old audit logs", oldLogs.size());
    }

    @Override
    @Transactional(readOnly = true)
    public long countAuditLogsSince(LocalDateTime date) {
        return auditLogRepository.countAuditLogsSince(date);
    }

    private AuditLog createAuditLog(String userIdentifier,
                                    String action,
                                    String entityType,
                                    String entityId,
                                    String details) {

        AuditLog auditLog = new AuditLog();

        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setStatus("SUCCESS");

        if (userIdentifier != null && !userIdentifier.isBlank()) {

            Optional<User> user = userRepository.findByEmail(userIdentifier);

            if (user.isEmpty()) {
                user = userRepository.findById(userIdentifier);
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
}