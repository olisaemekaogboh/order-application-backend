package com.inkfront.logisticsApplication.validator.dispatch;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.exception.dispatch.DriverUnavailableException;
import com.inkfront.logisticsApplication.exception.dispatch.VehicleUnavailableException;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DispatchAssignmentValidator {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public void validateDriverAvailable(String driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
        if (!driver.getAvailable()) {
            throw new DriverUnavailableException("Driver is not available");
        }
        // Check if driver already has active dispatch (you can query dispatch repository)
    }

    public void validateVehicleAvailable(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        if (!vehicle.isAvailable()) {
            throw new VehicleUnavailableException("Vehicle is not available");
        }
    }
}