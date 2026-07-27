package com.inkfront.logisticsApplication.dto.request.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditSearchRequestDTO {

    private String userId;
    private String username;
    private String entityType;
    private String entityId;
    private String action;          // now String, not enum
    private LocalDate startDate;
    private LocalDate endDate;

    // Pagination & sorting defaults
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "timestamp";
    private String sortDirection = "DESC";
}