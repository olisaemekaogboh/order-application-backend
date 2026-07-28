package com.inkfront.logisticsApplication.repository.tracking;

import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, String> {

    List<TrackingEvent> findByTrackingSessionIdOrderByTimestampAsc(String sessionId);

    Page<TrackingEvent> findByTrackingSessionId(String sessionId, Pageable pageable);

    @Query("SELECT te FROM TrackingEvent te WHERE te.trackingSession.id = :sessionId AND te.eventType = 'STATUS_CHANGE' ORDER BY te.timestamp ASC")
    List<TrackingEvent> findStatusChangeEvents(@Param("sessionId") String sessionId);
}