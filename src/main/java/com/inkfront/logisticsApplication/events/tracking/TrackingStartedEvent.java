package com.inkfront.logisticsApplication.events.tracking;

import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingSession;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TrackingStartedEvent extends ApplicationEvent {
    private final TrackingSession trackingSession;

    public TrackingStartedEvent(Object source, TrackingSession trackingSession) {
        super(source);
        this.trackingSession = trackingSession;
    }
}