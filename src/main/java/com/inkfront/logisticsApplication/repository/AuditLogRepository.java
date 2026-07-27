package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    Page<AuditLog> findByUserId(String userId, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    Page<AuditLog> findByEntityId(String entityId, Pageable pageable);

    List<AuditLog> findByUserIdAndTimestampBetween(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("SELECT al FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findAuditLogsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT al FROM AuditLog al WHERE al.action LIKE %:action% AND al.timestamp >= :date")
    List<AuditLog> searchAuditLogs(
            @Param("action") String action,
            @Param("date") LocalDateTime date
    );

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.timestamp >= :date")
    long countAuditLogsSince(@Param("date") LocalDateTime date);
    Page<AuditLog> findByEntityTypeAndEntityId(
            String entityType,
            String entityId,
            Pageable pageable
    );

}
