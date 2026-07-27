package com.inkfront.logisticsApplication.dto.response.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditRetentionDTO {

    private Integer retentionDays;
    private String message;
}