package com.inkfront.logisticsApplication.service.impl.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.vehicle.VehicleNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleNotificationServiceImpl implements VehicleNotificationService {

    private final VehicleRepository vehicleRepository;
    private final NotificationService notificationService;

    @Override
    @Scheduled(cron = "0 0 8 * * *") // daily at 8 AM
    public void sendMaintenanceReminders() {
        log.info("Checking vehicles due for maintenance");
        List<Vehicle> dueVehicles = vehicleRepository.findVehiclesDueForMaintenance(LocalDate.now().plusDays(7));
        for (Vehicle vehicle : dueVehicles) {
            // Send to fleet managers
            notificationService.sendSystemNotification("FLEET_MANAGER", "Maintenance Due",
                    "Vehicle " + vehicle.getVehicleNumber() + " is due for maintenance on " + vehicle.getNextMaintenanceDate());
        }
    }

    @Override
    @Scheduled(cron = "0 0 8 * * *")
    public void sendInspectionReminders() {
        log.info("Checking vehicles due for inspection");
        List<Vehicle> dueVehicles = vehicleRepository.findVehiclesDueForInspection(LocalDate.now().plusDays(7));
        for (Vehicle vehicle : dueVehicles) {
            notificationService.sendSystemNotification("FLEET_MANAGER", "Inspection Due",
                    "Vehicle " + vehicle.getVehicleNumber() + " is due for inspection on " + vehicle.getNextInspectionDate());
        }
    }

    @Override
    @Scheduled(cron = "0 0 8 * * *")
    public void sendInsuranceReminders() {
        log.info("Checking vehicles with expiring insurance");
        List<Vehicle> expiring = vehicleRepository.findVehiclesWithExpiringInsurance(LocalDate.now().plusDays(30));
        for (Vehicle vehicle : expiring) {
            notificationService.sendSystemNotification("FLEET_MANAGER", "Insurance Expiry",
                    "Insurance for vehicle " + vehicle.getVehicleNumber() + " expires on " + vehicle.getInsuranceExpiry());
        }
    }

    @Override
    public void notifyVehicleAssigned(Vehicle vehicle, String driverName) {
        // Called from event listener
        notificationService.sendSystemNotification(vehicle.getId(), "Vehicle Assigned",
                "Vehicle " + vehicle.getVehicleNumber() + " assigned to " + driverName);
    }
}