package com.inkfront.logisticsApplication.listener;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.entity.vehicle.VehicleAssignment;
import com.inkfront.logisticsApplication.events.vehicle.*;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleAssignmentRepository;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleEventListener {

    private final NotificationService notificationService;
    private final AuditService auditService;
    private final VehicleAssignmentRepository assignmentRepository;

    @Async
    @EventListener
    public void handleVehicleCreated(VehicleCreatedEvent event) {
        Vehicle vehicle = event.getVehicle();
        log.info("Handling VehicleCreatedEvent for vehicle: {}", vehicle.getId());
        // Send notifications, etc.
        notificationService.sendSystemNotification("FLEET_MANAGER", "Vehicle Created",
                "Vehicle " + vehicle.getVehicleNumber() + " has been added to the fleet.");
    }

    @Async
    @EventListener
    public void handleVehicleAssigned(VehicleAssignedEvent event) {
        Vehicle vehicle = event.getVehicle();
        log.info("Handling VehicleAssignedEvent for vehicle: {}", vehicle.getId());

        // Get current driver from assignment
        String driverName = getCurrentDriverName(vehicle.getId());

        // Send notifications
        notificationService.sendSystemNotification(vehicle.getId(), "Vehicle Assigned",
                "Vehicle " + vehicle.getVehicleNumber() + " has been assigned to " + driverName);
        notificationService.sendSystemNotification("FLEET_MANAGER", "Vehicle Assignment",
                "Vehicle " + vehicle.getVehicleNumber() + " assigned to " + driverName);
    }

    @Async
    @EventListener
    public void handleVehicleReleased(VehicleReleasedEvent event) {
        Vehicle vehicle = event.getVehicle();
        log.info("Handling VehicleReleasedEvent for vehicle: {}", vehicle.getId());

        notificationService.sendSystemNotification("FLEET_MANAGER", "Vehicle Released",
                "Vehicle " + vehicle.getVehicleNumber() + " has been released from assignment.");
    }

    @Async
    @EventListener
    public void handleVehicleMaintenanceStarted(VehicleMaintenanceStartedEvent event) {
        Vehicle vehicle = event.getVehicle();
        log.info("Handling VehicleMaintenanceStartedEvent for vehicle: {}", vehicle.getId());

        notificationService.sendSystemNotification("FLEET_MANAGER", "Maintenance Started",
                "Maintenance started for vehicle " + vehicle.getVehicleNumber());
    }

    @Async
    @EventListener
    public void handleVehicleMaintenanceCompleted(VehicleMaintenanceCompletedEvent event) {
        Vehicle vehicle = event.getVehicle();
        log.info("Handling VehicleMaintenanceCompletedEvent for vehicle: {}", vehicle.getId());

        notificationService.sendSystemNotification("FLEET_MANAGER", "Maintenance Completed",
                "Maintenance completed for vehicle " + vehicle.getVehicleNumber());
    }

    @Async
    @EventListener
    public void handleVehicleInspectionCompleted(VehicleInspectionCompletedEvent event) {
        Vehicle vehicle = event.getVehicle();
        log.info("Handling VehicleInspectionCompletedEvent for vehicle: {}", vehicle.getId());

        notificationService.sendSystemNotification("FLEET_MANAGER", "Inspection Completed",
                "Inspection completed for vehicle " + vehicle.getVehicleNumber());
    }

    @Async
    @EventListener
    public void handleVehicleRetired(VehicleRetiredEvent event) {
        Vehicle vehicle = event.getVehicle();
        log.info("Handling VehicleRetiredEvent for vehicle: {}", vehicle.getId());

        notificationService.sendSystemNotification("FLEET_MANAGER", "Vehicle Retired",
                "Vehicle " + vehicle.getVehicleNumber() + " has been retired from the fleet.");
    }

    // Helper to get current driver name from active assignment
    private String getCurrentDriverName(String vehicleId) {
        Optional<VehicleAssignment> assignment = assignmentRepository.findByVehicleIdAndActiveTrue(vehicleId);
        return assignment.map(a -> a.getDriver().getUser().getFullName()).orElse("Unknown");
    }
}