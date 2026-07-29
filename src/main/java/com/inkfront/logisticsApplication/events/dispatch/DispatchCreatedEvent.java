package com.inkfront.logisticsApplication.events.dispatch;

import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DispatchCreatedEvent extends ApplicationEvent {
    private final Dispatch dispatch;

    public DispatchCreatedEvent(Object source, Dispatch dispatch) {
        super(source);
        this.dispatch = dispatch;
    }
}