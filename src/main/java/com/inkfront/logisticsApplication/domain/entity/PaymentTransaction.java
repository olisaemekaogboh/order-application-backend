package com.inkfront.logisticsApplication.domain.entity;
import com.inkfront.logisticsApplication.domain.enums.PaymentMethod;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "transaction_reference", nullable = false, unique = true)
    private String transactionReference;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "currency")
    private String currency = "NGN";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "gateway_reference")
    private String gatewayReference;

    @Column(name = "gateway_response")
    private String gatewayResponse;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_data", columnDefinition = "jsonb")
    private String paymentData;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "last_retry_date")
    private LocalDateTime lastRetryDate;
}