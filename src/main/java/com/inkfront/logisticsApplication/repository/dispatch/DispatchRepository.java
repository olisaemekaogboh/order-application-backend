package com.inkfront.logisticsApplication.repository.dispatch;

import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.domain.enums.DispatchStatus;
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
public interface DispatchRepository extends JpaRepository<Dispatch, String> {

    Optional<Dispatch> findByOrderId(String orderId);

    List<Dispatch> findByDriverId(String driverId);

    Page<Dispatch> findByDriverId(String driverId, Pageable pageable);

    List<Dispatch> findByVehicleId(String vehicleId);

    Page<Dispatch> findByVehicleId(String vehicleId, Pageable pageable);

    Page<Dispatch> findByStatus(DispatchStatus status, Pageable pageable);

    @Query("SELECT d FROM Dispatch d WHERE d.status = 'PENDING' OR d.status = 'SEARCHING_DRIVER' OR d.status = 'SEARCHING_VEHICLE'")
    List<Dispatch> findPendingDispatches();

    @Query("SELECT d FROM Dispatch d WHERE d.status = 'PENDING' ORDER BY d.priority DESC, d.createdAt ASC")
    List<Dispatch> findPendingDispatchesOrderedByPriority();

    @Query("SELECT d FROM Dispatch d WHERE d.scheduledTime <= :now AND d.status = 'PENDING'")
    List<Dispatch> findScheduledDispatchesDue(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(d) FROM Dispatch d WHERE d.status = :status")
    long countByStatus(@Param("status") DispatchStatus status);

    @Query("SELECT COUNT(d) FROM Dispatch d WHERE d.createdAt BETWEEN :start AND :end")
    long countCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, d.createdAt, d.completedAt)) FROM Dispatch d WHERE d.status = 'COMPLETED' AND d.completedAt IS NOT NULL")
    Double averageDispatchCompletionTime();

    // New methods for assignment validation
    boolean existsByDriverIdAndStatusIn(String driverId, List<DispatchStatus> statuses);

    boolean existsByVehicleIdAndStatusIn(String vehicleId, List<DispatchStatus> statuses);

    @Query("SELECT d FROM Dispatch d WHERE d.driverId = :driverId AND d.status = 'DRIVER_ACCEPTED'")
    List<Dispatch> findActiveDispatchesByDriverId(@Param("driverId") String driverId);

    @Query("SELECT d FROM Dispatch d WHERE d.vehicleId = :vehicleId AND d.status IN ('DRIVER_ACCEPTED', 'EN_ROUTE_PICKUP', 'DELIVERY_IN_PROGRESS')")
    List<Dispatch> findActiveDispatchesByVehicleId(@Param("vehicleId") String vehicleId);
}