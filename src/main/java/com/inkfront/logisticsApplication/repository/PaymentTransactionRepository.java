package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.PaymentTransaction;
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

    Optional<PaymentTransaction> findByOrderId(String orderId);

    List<PaymentTransaction> findByStatus(PaymentStatus status);

    List<PaymentTransaction> findByPaymentMethod(PaymentMethod paymentMethod);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.order.user.id = :userId")
    List<PaymentTransaction> findByUserId(@Param("userId") String userId);

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

    long countByStatus(PaymentStatus status);
    // repository/PaymentTransactionRepository.java - Add these methods

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.status = :status")
    Double sumAmountByStatus(@Param("status") PaymentStatus status);
}