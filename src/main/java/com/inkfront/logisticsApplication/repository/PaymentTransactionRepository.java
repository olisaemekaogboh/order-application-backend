package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.domain.enums.PaymentMethod;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {

    Optional<PaymentTransaction> findByTransactionReference(String transactionReference);

    Optional<PaymentTransaction> findByGatewayReference(String gatewayReference);

    Optional<PaymentTransaction> findByOrderId(String orderId);

    List<PaymentTransaction> findByStatus(PaymentStatus status);

    List<PaymentTransaction> findByPaymentMethod(PaymentMethod paymentMethod);

    List<PaymentTransaction> findByGateway(PaymentGateway gateway);

    List<PaymentTransaction> findByOrderUserId(String userId);

    Page<PaymentTransaction> findByOrderUserId(String userId, Pageable pageable);

    Page<PaymentTransaction> findByStatus(PaymentStatus status, Pageable pageable);

    Page<PaymentTransaction> findByGateway(PaymentGateway gateway, Pageable pageable);

    Page<PaymentTransaction> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.paymentDate BETWEEN :startDate AND :endDate")
    List<PaymentTransaction> findTransactionsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.paymentDate BETWEEN :startDate AND :endDate")
    Page<PaymentTransaction> findTransactionsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = 'PENDING' AND pt.createdAt < :date")
    List<PaymentTransaction> findPendingTransactionsOlderThan(@Param("date") LocalDateTime date);

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.status = 'PAID' AND pt.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumSuccessfulPaymentsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    long countByStatus(PaymentStatus status);

    boolean existsByTransactionReference(String transactionReference);

    @Query("SELECT COUNT(pt) FROM PaymentTransaction pt WHERE pt.order.user.id = :userId")
    long countByUserId(@Param("userId") String userId);

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.status = 'PAID'")
    BigDecimal sumSuccessfulPayments();
}