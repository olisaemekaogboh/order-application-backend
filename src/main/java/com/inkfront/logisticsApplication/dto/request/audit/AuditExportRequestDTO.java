package com.inkfront.logisticsApplication.dto.request.audit;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditExportRequestDTO {

    private String action;          // String, not enum
    private LocalDate startDate;
    private LocalDate endDate;

    @NotNull(message = "Export format is required")
    private String format;          // e.g., "CSV", "PDF", "EXCEL"
}