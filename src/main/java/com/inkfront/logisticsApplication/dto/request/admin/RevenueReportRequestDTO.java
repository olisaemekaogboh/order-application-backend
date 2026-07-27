// dto/request/admin/RevenueReportRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.admin;

import com.inkfront.logisticsApplication.domain.enums.ReportPeriod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RevenueReportRequestDTO {

    @NotNull(message = "Report period is required")
    private ReportPeriod period;

    private LocalDate startDate;
    private LocalDate endDate;

    private String userId;
    private String driverId;
    private String currency = "NGN";
    private boolean includeBreakdown = true;
}