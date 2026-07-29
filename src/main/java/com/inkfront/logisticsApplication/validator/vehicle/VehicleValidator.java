package com.inkfront.logisticsApplication.validator.vehicle;

import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleValidator {

    private final VehicleRepository vehicleRepository;

    public void validateUniqueVehicleNumber(String vehicleNumber) {
        if (vehicleRepository.existsByVehicleNumber(vehicleNumber)) {
            throw new IllegalArgumentException("Vehicle number already exists: " + vehicleNumber);
        }
    }

    public void validateUniqueRegistrationNumber(String registrationNumber) {
        if (vehicleRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new IllegalArgumentException("Registration number already exists: " + registrationNumber);
        }
    }

    public void validateUniqueVin(String vin) {
        if (vin != null && vehicleRepository.existsByVin(vin)) {
            throw new IllegalArgumentException("VIN already exists: " + vin);
        }
    }

    public void validateVehicleExists(String vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new IllegalArgumentException("Vehicle not found: " + vehicleId);
        }
    }
}