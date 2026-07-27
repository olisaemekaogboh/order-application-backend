package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(String userId, Pageable pageable);

    Page<Order> findByDriverId(String driverId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable);

    List<Order> findByUserIdAndStatusIn(String userId, List<OrderStatus> statuses);

    List<Order> findByDriverIdAndStatusIn(String driverId, List<OrderStatus> statuses);

    long countByUserId(String userId);
    long countByUserIdAndStatusIn(
            String userId,
            List<OrderStatus> statuses
    );
    long countByDriverId(String driverId);

    long countByStatus(OrderStatus status);

    long countByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.orderDate < :date")
    List<Order> findStaleOrders(@Param("status") OrderStatus status, @Param("date") LocalDateTime date);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderDate BETWEEN :startDate AND :endDate")
    Page<Order> findOrdersByUserAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT o FROM Order o WHERE o.driver.id = :driverId AND o.orderDate BETWEEN :startDate AND :endDate")
    Page<Order> findOrdersByDriverAndDateRange(
            @Param("driverId") String driverId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.status = :status")
    Page<Order> findOrdersBetweenDatesAndStatus(
            LocalDateTime startDate,
            LocalDateTime endDate,
            OrderStatus status,
            Pageable pageable
    );

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED'")
    Double sumTotalPriceBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT o FROM Order o WHERE o.deliveryDate IS NULL AND o.estimatedDeliveryDate < :now AND o.status != 'DELIVERED' AND o.status != 'CANCELLED'")
    List<Order> findOverdueOrders(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = 'DELIVERED'")
    long countDeliveredOrdersByUser(@Param("userId") String userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = 'CANCELLED'")
    long countCancelledOrdersByUser(@Param("userId") String userId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findRecentOrdersByUser(@Param("userId") String userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
    void updateOrderStatus(@Param("orderId") String orderId, @Param("status") OrderStatus status);

    @Modifying
    @Query("UPDATE Order o SET o.paymentStatus = :paymentStatus WHERE o.id = :orderId")
    void updatePaymentStatus(@Param("orderId") String orderId, @Param("paymentStatus") PaymentStatus paymentStatus);
    @Query("""
       SELECT o
       FROM Order o
       WHERE o.orderDate BETWEEN :startDate
       AND :endDate
       AND o.status = :status
       """)
    List<Order> findOrdersBetweenDatesAndStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") OrderStatus status
    );
    // repository/OrderRepository.java - Add these methods

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    Long countOrdersBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.createdAt BETWEEN :start AND :end")
    Long countByUserIdAndDateRange(@Param("userId") String userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = :status")
    Double sumTotalPriceByStatus(@Param("status") OrderStatus status);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    Double sumTotalPriceByUserIdAndStatus(@Param("userId") String userId, @Param("status") OrderStatus status);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.user.id = :userId AND o.createdAt BETWEEN :start AND :end")
    Double sumTotalPriceByUserIdAndDateRange(@Param("userId") String userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    Long countByUserIdAndStatus(@Param("userId") String userId, @Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.deliveryDate BETWEEN :start AND :end")
    Long countDeliveredBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


}