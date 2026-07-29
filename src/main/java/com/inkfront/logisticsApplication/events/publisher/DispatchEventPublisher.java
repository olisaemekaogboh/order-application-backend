package com.inkfront.logisticsApplication.events.publisher;

import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.events.dispatch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishDispatchCreated(Dispatch dispatch) {
        eventPublisher.publishEvent(new DispatchCreatedEvent(this, dispatch));
        log.info("Published DispatchCreatedEvent for dispatch: {}", dispatch.getId());
    }

    public void publishDispatchAssigned(Dispatch dispatch) {
        eventPublisher.publishEvent(new DispatchAssignedEvent(this, dispatch));
        log.info("Published DispatchAssignedEvent for dispatch: {}", dispatch.getId());
    }

    public void publishDispatchAccepted(Dispatch dispatch) {
        eventPublisher.publishEvent(new DispatchAcceptedEvent(this, dispatch));
        log.info("Published DispatchAcceptedEvent for dispatch: {}", dispatch.getId());
    }

    public void publishDispatchRejected(Dispatch dispatch) {
        eventPublisher.publishEvent(new DispatchRejectedEvent(this, dispatch));
        log.info("Published DispatchRejectedEvent for dispatch: {}", dispatch.getId());
    }

    public void publishDispatchCompleted(Dispatch dispatch) {
        eventPublisher.publishEvent(new DispatchCompletedEvent(this, dispatch));
        log.info("Published DispatchCompletedEvent for dispatch: {}", dispatch.getId());
    }

    public void publishDispatchCancelled(Dispatch dispatch) {
        eventPublisher.publishEvent(new DispatchCancelledEvent(this, dispatch));
        log.info("Published DispatchCancelledEvent for dispatch: {}", dispatch.getId());
    }
}