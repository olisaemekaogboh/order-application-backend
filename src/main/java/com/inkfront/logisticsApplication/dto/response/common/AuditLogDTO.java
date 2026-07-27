// dto/response/common/AuditLogDTO.java
package com.inkfront.logisticsApplication.dto.response.common;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogDTO {

    private String id;
    private String userId;
    private String username;
    private String email;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String status;
    private String errorMessage;
    private Long durationMs;
    private String requestUrl;
    private String httpMethod;
    private String formattedTimestamp;
}