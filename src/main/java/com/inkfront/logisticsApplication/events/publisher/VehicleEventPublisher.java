package com.inkfront.logisticsApplication.events.publisher;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.events.vehicle.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishVehicleCreated(Vehicle vehicle) {
        eventPublisher.publishEvent(new VehicleCreatedEvent(this, vehicle));
        log.info("Published VehicleCreatedEvent for vehicle: {}", vehicle.getId());
    }

    public void publishVehicleAssigned(Vehicle vehicle) {
        eventPublisher.publishEvent(new VehicleAssignedEvent(this, vehicle));
        log.info("Published VehicleAssignedEvent for vehicle: {}", vehicle.getId());
    }

    public void publishVehicleReleased(Vehicle vehicle) {
        eventPublisher.publishEvent(new VehicleReleasedEvent(this, vehicle));
        log.info("Published VehicleReleasedEvent for vehicle: {}", vehicle.getId());
    }

    public void publishVehicleMaintenanceStarted(Vehicle vehicle) {
        eventPublisher.publishEvent(new VehicleMaintenanceStartedEvent(this, vehicle));
        log.info("Published VehicleMaintenanceStartedEvent for vehicle: {}", vehicle.getId());
    }

    public void publishVehicleMaintenanceCompleted(Vehicle vehicle) {
        eventPublisher.publishEvent(new VehicleMaintenanceCompletedEvent(this, vehicle));
        log.info("Published VehicleMaintenanceCompletedEvent for vehicle: {}", vehicle.getId());
    }

    public void publishVehicleInspectionCompleted(Vehicle vehicle) {
        eventPublisher.publishEvent(new VehicleInspectionCompletedEvent(this, vehicle));
        log.info("Published VehicleInspectionCompletedEvent for vehicle: {}", vehicle.getId());
    }

    public void publishVehicleRetired(Vehicle vehicle) {
        eventPublisher.publishEvent(new VehicleRetiredEvent(this, vehicle));
        log.info("Published VehicleRetiredEvent for vehicle: {}", vehicle.getId());
    }
}