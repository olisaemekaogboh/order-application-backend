// dto/response/admin/SystemConfigDTO.java
package com.inkfront.logisticsApplication.dto.response.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemConfigDTO {

    private String id;
    private String configKey;
    private String configValue;
    private String description;
    private String category;
    private boolean encrypted;
    private boolean publicAccess;
    private String validationRules;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}