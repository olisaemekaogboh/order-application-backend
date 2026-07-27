package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.DriverEarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverEarningRepository extends JpaRepository<DriverEarning, String> {

    List<DriverEarning> findByDriverId(String driverId);

    Page<DriverEarning> findByDriverId(String driverId, Pageable pageable);

    List<DriverEarning> findByDriverIdAndPaidFalse(String driverId);

    List<DriverEarning> findByOrderId(String orderId);

    Optional<DriverEarning> findByOrderIdAndDriverId(String orderId, String driverId);

    @Query("SELECT de FROM DriverEarning de WHERE de.driver.id = :driverId AND de.earningDate BETWEEN :startDate AND :endDate")
    List<DriverEarning> findDriverEarningsBetweenDates(
            @Param("driverId") String driverId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(de.amount) FROM DriverEarning de WHERE de.driver.id = :driverId AND de.paid = false")
    Double sumUnpaidEarnings(@Param("driverId") String driverId);

    @Query("SELECT SUM(de.amount) FROM DriverEarning de WHERE de.driver.id = :driverId")
    Double sumTotalEarnings(@Param("driverId") String driverId);

    @Query("SELECT SUM(de.netAmount) FROM DriverEarning de WHERE de.driver.id = :driverId AND de.paid = false")
    Double sumUnpaidNetEarnings(@Param("driverId") String driverId);

    @Query("SELECT de FROM DriverEarning de WHERE de.paid = false AND de.earningDate < :date")
    List<DriverEarning> findUnpaidEarningsOlderThan(@Param("date") LocalDateTime date);

    @Query("SELECT SUM(de.amount) FROM DriverEarning de WHERE de.earningDate BETWEEN :startDate AND :endDate")
    Double sumAllEarningsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(de.commission) FROM DriverEarning de WHERE de.earningDate BETWEEN :startDate AND :endDate")
    Double sumAllCommissionsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    long countByDriverIdAndPaidFalse(String driverId);
}