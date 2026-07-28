package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
import com.inkfront.logisticsApplication.domain.enums.PaymentGateway;
import com.inkfront.logisticsApplication.domain.enums.PaymentMethod;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    /**
     * Find all transactions belonging to a specific user (via order.user.id).
     * Spring Data JPA derives the query from the method name.
     */
    List<PaymentTransaction> findByOrderUserId(String userId);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.paymentDate BETWEEN :startDate AND :endDate")
    List<PaymentTransaction> findTransactionsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = 'PENDING' AND pt.createdAt < :date")
    List<PaymentTransaction> findPendingTransactionsOlderThan(@Param("date") LocalDateTime date);

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.status = 'PAID' AND pt.paymentDate BETWEEN :startDate AND :endDate")
    Double sumSuccessfulPaymentsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.status = :status")
    Double sumAmountByStatus(@Param("status") PaymentStatus status);

    long countByStatus(PaymentStatus status);

    boolean existsByTransactionReference(String transactionReference);

    @Query("SELECT COUNT(pt) FROM PaymentTransaction pt WHERE pt.order.user.id = :userId")
    long countByUserId(@Param("userId") String userId);

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.status = 'PAID'")
    Double sumSuccessfulPayments();
}