package com.inkfront.logisticsApplication.listener;

import com.inkfront.logisticsApplication.events.dispatch.*;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import com.inkfront.logisticsApplication.service.interfaces.dispatch.DispatchNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchEventListener {

    private final DispatchNotificationService notificationService;
    private final AuditService auditService;

    @Async
    @EventListener
    @Transactional
    public void handleDispatchCreated(DispatchCreatedEvent event) {
        log.info("Handling DispatchCreatedEvent for dispatch: {}", event.getDispatch().getId());
        notificationService.notifyDispatchCreated(event.getDispatch());
    }

    @Async
    @EventListener
    @Transactional
    public void handleDispatchAssigned(DispatchAssignedEvent event) {
        log.info("Handling DispatchAssignedEvent for dispatch: {}", event.getDispatch().getId());
        notificationService.notifyDispatchAssigned(event.getDispatch());
    }

    @Async
    @EventListener
    @Transactional
    public void handleDispatchAccepted(DispatchAcceptedEvent event) {
        log.info("Handling DispatchAcceptedEvent for dispatch: {}", event.getDispatch().getId());
        notificationService.notifyDispatchAccepted(event.getDispatch());
    }

    @Async
    @EventListener
    @Transactional
    public void handleDispatchRejected(DispatchRejectedEvent event) {
        log.info("Handling DispatchRejectedEvent for dispatch: {}", event.getDispatch().getId());
        notificationService.notifyDispatchRejected(event.getDispatch(), "Rejected");
    }

    @Async
    @EventListener
    @Transactional
    public void handleDispatchCompleted(DispatchCompletedEvent event) {
        log.info("Handling DispatchCompletedEvent for dispatch: {}", event.getDispatch().getId());
        notificationService.notifyDispatchCompleted(event.getDispatch());
    }

    @Async
    @EventListener
    @Transactional
    public void handleDispatchCancelled(DispatchCancelledEvent event) {
        log.info("Handling DispatchCancelledEvent for dispatch: {}", event.getDispatch().getId());
        notificationService.notifyDispatchCancelled(event.getDispatch(), "Cancelled");
    }
}