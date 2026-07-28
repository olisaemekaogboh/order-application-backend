package com.inkfront.logisticsApplication.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummaryDTO {

    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String generatedBy;
    private LocalDate generatedAt;
}