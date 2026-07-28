package com.inkfront.logisticsApplication.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerReportDTO {

    private ReportSummaryDTO summary;
    private Long totalCustomers;
    private Long newCustomers;
    private Long returningCustomers;
    private List<String> topCustomers;
    private List<String> mostActiveCustomers;
    private Double averageSpend;
    private Long completedOrders;
    private Long cancelledOrders;
    private Map<String, Long> customersByActivity;
    private LocalDate generatedAt;
}