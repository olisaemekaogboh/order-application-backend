package com.inkfront.logisticsApplication.validator.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.exception.vehicle.MaintenanceException;
import org.springframework.stereotype.Component;

@Component
public class VehicleMaintenanceValidator {

    public void validateVehicleCanGoForMaintenance(Vehicle vehicle) {
        if (vehicle.getStatus() == VehicleStatus.RETIRED) {
            throw new MaintenanceException("Vehicle is retired and cannot go for maintenance");
        }
        if (vehicle.getStatus() == VehicleStatus.UNDER_MAINTENANCE) {
            throw new MaintenanceException("Vehicle is already under maintenance");
        }
    }
}