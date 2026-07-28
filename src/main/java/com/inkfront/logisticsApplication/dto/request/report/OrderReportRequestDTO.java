package com.inkfront.logisticsApplication.dto.request.report;

import com.inkfront.logisticsApplication.domain.enums.ReportFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReportRequestDTO {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private String status;
    private String driverId;
    private String customerId;
    private String vehicleType;
    private ReportFormat reportFormat = ReportFormat.PDF;
    private boolean includeCharts;
    private boolean includeSummary = true;
}