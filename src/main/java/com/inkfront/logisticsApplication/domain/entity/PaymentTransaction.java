package com.inkfront.logisticsApplication.domain.entity;

import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.domain.enums.PaymentMethod;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(
        callSuper = true,
        onlyExplicitlyIncluded = true
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction extends BaseEntity {

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "transaction_reference", nullable = false, unique = true)
    private String transactionReference;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "refunded_amount", precision = 19, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(name = "processing_fee", precision = 19, scale = 2)
    private BigDecimal processingFee = BigDecimal.ZERO;

    @Column(name = "gateway_fee", precision = 19, scale = 2)
    private BigDecimal gatewayFee = BigDecimal.ZERO;

    @Column(name = "currency")
    private String currency = "NGN";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway", nullable = false)
    private PaymentGateway gateway;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "gateway_reference")
    private String gatewayReference;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Column(name = "authorization_code")
    private String authorizationCode;

    @Column(name = "channel")
    private String channel;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "authorization_url")
    private String authorizationUrl;

    @Column(name = "access_code")
    private String accessCode;

    @Column(name = "callback_url")
    private String callbackUrl;

    // ===== JSONB FIELDS - Keep as String but add JSONB column definition =====

    @Type(JsonStringType.class)
    @Column(name = "gateway_response", columnDefinition = "jsonb")
    private String gatewayResponse;

    @Type(JsonStringType.class)
    @Column(name = "payment_data", columnDefinition = "jsonb")
    private String paymentData;

    @Type(JsonStringType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    // ===== END JSONB FIELDS =====

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    @Column(name = "next_retry_date")
    private LocalDateTime nextRetryDate;

    @Column(name = "last_retry_date")
    private LocalDateTime lastRetryDate;

    public boolean isCompleted() {
        return this.status == PaymentStatus.PAID ||
                this.status == PaymentStatus.FAILED ||
                this.status == PaymentStatus.REFUNDED ||
                this.status == PaymentStatus.CANCELLED;
    }

    public boolean isSuccessful() {
        return this.status == PaymentStatus.PAID;
    }

    public boolean isRefundable() {
        return this.status == PaymentStatus.PAID && this.refundedAmount.compareTo(this.amount) < 0;
    }

    public BigDecimal getRemainingBalance() {
        return this.amount.subtract(this.refundedAmount);
    }
}