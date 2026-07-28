package com.inkfront.logisticsApplication.repository.tracking;

import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingSession;
import com.inkfront.logisticsApplication.domain.enums.TrackingStatus;
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
public interface TrackingSessionRepository extends JpaRepository<TrackingSession, String> {

    Optional<TrackingSession> findByOrderId(String orderId);

    List<TrackingSession> findByDriverId(String driverId);

    Page<TrackingSession> findByDriverId(String driverId, Pageable pageable);

    Page<TrackingSession> findByStatus(TrackingStatus status, Pageable pageable);

    @Query("SELECT ts FROM TrackingSession ts WHERE ts.order.user.id = :userId")
    Page<TrackingSession> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT ts FROM TrackingSession ts WHERE ts.status = 'IN_TRANSIT' OR ts.status = 'PICKED_UP'")
    List<TrackingSession> findActiveSessions();

    @Query("SELECT ts FROM TrackingSession ts WHERE ts.estimatedArrival < :now AND ts.status NOT IN ('DELIVERED', 'CANCELLED', 'FAILED')")
    List<TrackingSession> findOverdueSessions(@Param("now") LocalDateTime now);
}