package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.AuditLog;
import com.inkfront.logisticsApplication.dto.response.common.AuditLogDTO;
import org.mapstruct.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AuditLogMapper {

    @Mapping(target = "formattedTimestamp", expression = "java(formatTimestamp(auditLog.getTimestamp()))")
    public abstract AuditLogDTO toDTO(AuditLog auditLog);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract AuditLog toEntity(AuditLogDTO auditLogDTO);

    public abstract List<AuditLogDTO> toDTOList(List<AuditLog> auditLogs);

    protected String formatTimestamp(java.time.LocalDateTime timestamp) {
        if (timestamp == null) return null;
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}