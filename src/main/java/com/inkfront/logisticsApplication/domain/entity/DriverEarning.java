package com.inkfront.logisticsApplication.domain.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "driver_earnings")
public class DriverEarning extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "commission", nullable = false)
    private Double commission;

    @Column(name = "net_amount", nullable = false)
    private Double netAmount;

    @Column(name = "currency")
    private String currency = "NGN";

    @Column(name = "earning_date", nullable = false)
    private LocalDateTime earningDate;

    @Column(name = "paid")
    private boolean paid = false;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "notes")
    private String notes;

    @Column(name = "commission_rate")
    private Double commissionRate;


}
