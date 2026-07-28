package com.inkfront.logisticsApplication.events.publisher;

import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingSession;
import com.inkfront.logisticsApplication.events.tracking.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishTrackingStarted(TrackingSession session) {
        eventPublisher.publishEvent(new TrackingStartedEvent(this, session));
        log.info("Published TrackingStartedEvent for session: {}", session.getId());
    }

    public void publishTrackingLocationUpdated(TrackingSession session) {
        eventPublisher.publishEvent(new TrackingLocationUpdatedEvent(this, session));
        log.info("Published TrackingLocationUpdatedEvent for session: {}", session.getId());
    }

    public void publishTrackingStatusChanged(TrackingSession session) {
        eventPublisher.publishEvent(new TrackingStatusChangedEvent(this, session));
        log.info("Published TrackingStatusChangedEvent for session: {}", session.getId());
    }

    public void publishTrackingCompleted(TrackingSession session) {
        eventPublisher.publishEvent(new TrackingCompletedEvent(this, session));
        log.info("Published TrackingCompletedEvent for session: {}", session.getId());
    }

    public void publishTrackingCancelled(TrackingSession session) {
        eventPublisher.publishEvent(new TrackingCancelledEvent(this, session));
        log.info("Published TrackingCancelledEvent for session: {}", session.getId());
    }
}