package com.inkfront.logisticsApplication.events.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class VehicleInspectionCompletedEvent extends ApplicationEvent {
    private final Vehicle vehicle;

    public VehicleInspectionCompletedEvent(Object source, Vehicle vehicle) {
        super(source);
        this.vehicle = vehicle;
    }
}