package com.inkfront.logisticsApplication.validator.vehicle;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.exception.vehicle.VehicleAlreadyAssignedException;
import com.inkfront.logisticsApplication.exception.vehicle.VehicleUnavailableException;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleAssignmentValidator {

    private final VehicleAssignmentRepository assignmentRepository;

    public void validateVehicleAvailable(Vehicle vehicle) {
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE &&
                vehicle.getStatus() != VehicleStatus.INSPECTION_DUE) {
            throw new VehicleUnavailableException("Vehicle is not available for assignment. Current status: " + vehicle.getStatus());
        }

        if (assignmentRepository.existsByVehicleIdAndActiveTrue(vehicle.getId())) {
            throw new VehicleAlreadyAssignedException("Vehicle is already assigned to a driver");
        }
    }

    public void validateDriverNotAlreadyAssigned(Driver driver) {
        if (assignmentRepository.findByDriverIdAndActiveTrue(driver.getId()).isPresent()) {
            throw new VehicleAlreadyAssignedException("Driver is already assigned to a vehicle");
        }
    }
}