package com.inkfront.logisticsApplication.dto.response.revenue;

import com.inkfront.logisticsApplication.domain.enums.PaymentMethod;
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
public class PaymentReportDTO {

    private Double totalAmount;
    private Long totalTransactions;
    private Map<PaymentStatus, Long> transactionCountByStatus;
    private Map<PaymentMethod, Double> amountByMethod;
    private Double successRate;
    private String currency;
}