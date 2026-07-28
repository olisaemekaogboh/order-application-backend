package com.inkfront.logisticsApplication.repository.tracking;

import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingLocationRepository extends JpaRepository<TrackingLocation, String> {

    List<TrackingLocation> findByTrackingSessionIdOrderByTimestampDesc(String sessionId);

    Page<TrackingLocation> findByTrackingSessionId(String sessionId, Pageable pageable);

    @Query("SELECT tl FROM TrackingLocation tl WHERE tl.trackingSession.id = :sessionId AND tl.current = true")
    Optional<TrackingLocation> findCurrentLocationBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT tl FROM TrackingLocation tl WHERE tl.trackingSession.id = :sessionId AND tl.timestamp >= :since")
    List<TrackingLocation> findLocationsSince(@Param("sessionId") String sessionId, @Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM TrackingLocation tl WHERE tl.timestamp < :cutoff")
    void deleteByTimestampBefore(@Param("cutoff") LocalDateTime cutoff);

    void deleteByTrackingSessionId(String sessionId);
}