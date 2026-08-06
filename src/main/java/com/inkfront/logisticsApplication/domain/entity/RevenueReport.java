package com.inkfront.logisticsApplication.domain.entity;



import com.inkfront.logisticsApplication.domain.enums.ReportPeriod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(
        callSuper = true,
        onlyExplicitlyIncluded = true
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "revenue_reports")
public class RevenueReport extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "report_period", nullable = false)
    private ReportPeriod reportPeriod;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_revenue", nullable = false)
    private Double totalRevenue;

    @Column(name = "total_orders")
    private Long totalOrders;

    @Column(name = "average_order_value")
    private Double averageOrderValue;

    @Column(name = "total_commission")
    private Double totalCommission;

    @Column(name = "total_driver_payout")
    private Double totalDriverPayout;

    @Column(name = "currency")
    private String currency = "NGN";

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "generated_by")
    private String generatedBy;

    @Column(name = "report_data", columnDefinition = "jsonb")
    private String reportData;

    @Column(name = "report_name")
    private String reportName;

    @Column(name = "is_archived")
    private boolean archived = false;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
}