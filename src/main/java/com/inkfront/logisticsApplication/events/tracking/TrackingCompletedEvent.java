package com.inkfront.logisticsApplication.events.tracking;

import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingSession;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TrackingCompletedEvent extends ApplicationEvent {
    private final TrackingSession trackingSession;

    public TrackingCompletedEvent(Object source, TrackingSession trackingSession) {
        super(source);
        this.trackingSession = trackingSession;
    }
}