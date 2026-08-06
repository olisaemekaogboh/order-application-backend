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

    // ==========================================================
    // Basic Queries
    // ==========================================================

    @Query("""
        SELECT d
        FROM Dispatch d
        WHERE d.order.id = :orderId
    """)
    Optional<Dispatch> findByOrderId(@Param("orderId") String orderId);

    List<Dispatch> findByDriverId(String driverId);

    Page<Dispatch> findByDriverId(String driverId, Pageable pageable);

    List<Dispatch> findByVehicleId(String vehicleId);

    Page<Dispatch> findByVehicleId(String vehicleId, Pageable pageable);

    Page<Dispatch> findByStatus(DispatchStatus status, Pageable pageable);

    // ==========================================================
    // Queue Queries
    // ==========================================================

    @Query("""
        SELECT d
        FROM Dispatch d
        WHERE d.status IN (
            'PENDING',
            'WAITING_DRIVER_ACCEPTANCE'
        )
    """)
    List<Dispatch> findPendingDispatches();

    @Query("""
        SELECT d
        FROM Dispatch d
        WHERE d.status = 'PENDING'
        ORDER BY d.priority DESC,
                 d.createdAt ASC
    """)
    List<Dispatch> findPendingDispatchesOrderedByPriority();

    @Query("""
        SELECT d
        FROM Dispatch d
        WHERE d.scheduledTime <= :now
          AND d.status = 'PENDING'
    """)
    List<Dispatch> findScheduledDispatchesDue(
            @Param("now") LocalDateTime now);

    // ==========================================================
    // Analytics
    // ==========================================================

    @Query("""
        SELECT COUNT(d)
        FROM Dispatch d
        WHERE d.status = :status
    """)
    long countByStatus(@Param("status") DispatchStatus status);

    @Query("""
        SELECT COUNT(d)
        FROM Dispatch d
        WHERE d.createdAt BETWEEN :start AND :end
    """)
    long countCreatedBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = """
SELECT AVG(EXTRACT(EPOCH FROM (completed_at - created_at)) / 60.0)
FROM dispatches
WHERE status = 'DELIVERED'
AND completed_at IS NOT NULL
""", nativeQuery = true)
    Double averageDispatchCompletionTime();

    // ==========================================================
    // Assignment Validation
    // ==========================================================

    boolean existsByDriverIdAndStatusIn(
            String driverId,
            List<DispatchStatus> statuses);

    boolean existsByVehicleIdAndStatusIn(
            String vehicleId,
            List<DispatchStatus> statuses);

    // ==========================================================
    // Active Dispatches
    // ==========================================================

    @Query("""
        SELECT d
        FROM Dispatch d
        WHERE d.driverId = :driverId
          AND d.status IN (
                'WAITING_DRIVER_ACCEPTANCE',
                'DRIVER_ACCEPTED',
                'EN_ROUTE_PICKUP',
                'PICKUP_COMPLETED',
                'DELIVERY_IN_PROGRESS'
          )
    """)
    List<Dispatch> findActiveDispatchesByDriverId(
            @Param("driverId") String driverId);

    @Query("""
        SELECT d
        FROM Dispatch d
        WHERE d.vehicleId = :vehicleId
          AND d.status IN (
                'WAITING_DRIVER_ACCEPTANCE',
                'DRIVER_ACCEPTED',
                'EN_ROUTE_PICKUP',
                'PICKUP_COMPLETED',
                'DELIVERY_IN_PROGRESS'
          )
    """)
    List<Dispatch> findActiveDispatchesByVehicleId(
            @Param("vehicleId") String vehicleId);

    @Query("""
SELECT d
FROM Dispatch d
WHERE d.driverId = :driverId
AND d.status='DELIVERED'
ORDER BY d.completedAt DESC
""")
    List<Dispatch> findCompletedDispatchesByDriverId(
            @Param("driverId") String driverId);

    @Query("""
SELECT d
FROM Dispatch d
WHERE d.driverId=:driverId
AND d.status IN (
'WAITING_DRIVER_ACCEPTANCE',
'DRIVER_ACCEPTED',
'EN_ROUTE_PICKUP',
'PICKUP_COMPLETED',
'DELIVERY_IN_PROGRESS'
)
ORDER BY d.createdAt DESC
""")
    Optional<Dispatch> findCurrentDispatch(
            @Param("driverId") String driverId);
}