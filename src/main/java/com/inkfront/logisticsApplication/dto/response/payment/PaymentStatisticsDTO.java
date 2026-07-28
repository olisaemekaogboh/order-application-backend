package com.inkfront.logisticsApplication.dto.response.payment;

import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatisticsDTO {

    private Long totalTransactions;
    private Double totalAmount;
    private Double successfulAmount;
    private Long pendingCount;
    private Long paidCount;
    private Long failedCount;
    private Long refundedCount;
    private Long cancelledCount;
    private Map<PaymentStatus, Long> countByStatus;
    private String currency;
}