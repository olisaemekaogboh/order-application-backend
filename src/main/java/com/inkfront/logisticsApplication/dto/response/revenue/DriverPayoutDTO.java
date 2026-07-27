package com.inkfront.logisticsApplication.dto.response.revenue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverPayoutDTO {

    private String payoutId;
    private String driverId;
    private Double amount;
    private String status;
    private String transactionReference;
    private LocalDateTime processedAt;
    private String remarks;
    private Double newBalance;
}