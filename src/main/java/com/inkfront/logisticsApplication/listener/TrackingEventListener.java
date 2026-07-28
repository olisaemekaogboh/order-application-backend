package com.inkfront.logisticsApplication.listener;

import com.inkfront.logisticsApplication.events.tracking.*;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingEventListener {

    private final NotificationService notificationService;
    private final AuditService auditService;

    @Async
    @EventListener
    public void handleTrackingStarted(TrackingStartedEvent event) {
        log.info("Handling TrackingStartedEvent for session: {}", event.getTrackingSession().getId());
        // Additional asynchronous processing
    }

    @Async
    @EventListener
    public void handleTrackingCompleted(TrackingCompletedEvent event) {
        log.info("Handling TrackingCompletedEvent for session: {}", event.getTrackingSession().getId());
        // Update statistics, etc.
    }
}