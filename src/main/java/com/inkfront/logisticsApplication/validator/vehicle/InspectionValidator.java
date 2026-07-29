package com.inkfront.logisticsApplication.validator.vehicle;

import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.exception.vehicle.InspectionException;
import org.springframework.stereotype.Component;

@Component
public class InspectionValidator {

    public void validateVehicleCanBeInspected(Vehicle vehicle) {
        if (vehicle.getStatus() == VehicleStatus.RETIRED) {
            throw new InspectionException("Vehicle is retired and cannot be inspected");
        }
    }
}