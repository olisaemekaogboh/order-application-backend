package com.inkfront.logisticsApplication.dto.request.audit;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditRetentionRequestDTO {

    @Positive(message = "Retention days must be positive")
    private Integer retentionDays;
}