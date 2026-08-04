package com.inkfront.logisticsApplication.service.impl.dispatch;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.domain.enums.DispatchStatus;
import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchAssignmentResult;
import com.inkfront.logisticsApplication.exception.dispatch.DriverUnavailableException;
import com.inkfront.logisticsApplication.exception.dispatch.VehicleUnavailableException;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import com.inkfront.logisticsApplication.repository.dispatch.DispatchRepository;
import com.inkfront.logisticsApplication.service.interfaces.dispatch.DispatchAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchAssignmentServiceImpl implements DispatchAssignmentService {

    private final DispatchRepository dispatchRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;

    /**
     * List of statuses that indicate a driver is actively on a dispatch
     */
    private static final List<DispatchStatus> ACTIVE_DISPATCH_STATUSES = List.of(
            DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
            DispatchStatus.DRIVER_ACCEPTED,
            DispatchStatus.EN_ROUTE_PICKUP,
            DispatchStatus.PICKUP_COMPLETED,
            DispatchStatus.DELIVERY_IN_PROGRESS
    );

    @Override
    public List<Driver> findAvailableDriversForDispatch(String orderId) {
        List<Driver> availableDrivers = driverRepository.findByAvailableTrueAndVerifiedTrue();

        return availableDrivers.stream()
                .filter(driver -> !dispatchRepository.existsByDriverIdAndStatusIn(
                        driver.getId(),
                        ACTIVE_DISPATCH_STATUSES))
                .collect(Collectors.toList());
    }

    @Override
    public List<Vehicle> findAvailableVehiclesForDispatch(String orderId) {
        return vehicleRepository.findByStatusAndDeletedFalse(VehicleStatus.AVAILABLE)
                .stream()
                .filter(vehicle -> !dispatchRepository.existsByVehicleIdAndStatusIn(
                        vehicle.getId(),
                        ACTIVE_DISPATCH_STATUSES))
                .collect(Collectors.toList());
    }

    @Override
    public Driver assignBestDriver(String orderId) {
        List<Driver> available = findAvailableDriversForDispatch(orderId);
        if (available.isEmpty()) {
            throw new DriverUnavailableException("No available drivers found for this dispatch");
        }

        // Choose driver with highest rating and lowest completed orders (spread work)
        return available.stream()
                .sorted(Comparator.comparingDouble(Driver::getRating).reversed()
                        .thenComparingInt(Driver::getCompletedOrders))
                .findFirst()
                .orElse(available.get(0));
    }

    @Override
    public Vehicle assignBestVehicle(String orderId) {
        List<Vehicle> available = findAvailableVehiclesForDispatch(orderId);
        if (available.isEmpty()) {
            throw new VehicleUnavailableException("No available vehicles found for this dispatch");
        }

        // Choose vehicle with lowest current mileage (spread usage)
        return available.stream()
                .min(Comparator.comparingDouble(Vehicle::getCurrentMileage))
                .orElse(available.get(0));
    }

    @Override
    public DispatchAssignmentResult assignBestDriverAndVehicle(Dispatch dispatch) {
        log.info("Assigning best driver and vehicle for dispatch: {}", dispatch.getId());

        DispatchAssignmentResult result = new DispatchAssignmentResult();
        result.setSuccess(false);

        try {
            Driver bestDriver = assignBestDriver(dispatch.getOrder().getId());
            if (bestDriver != null) {
                dispatch.setDriver(bestDriver);
                // driverId is a read-only field, managed by the relationship
                result.setDriverId(bestDriver.getId());
                result.setDriverName(bestDriver.getName());
            } else {
                result.setMessage("No suitable driver found");
                return result;
            }

            Vehicle bestVehicle = assignBestVehicle(dispatch.getOrder().getId());
            if (bestVehicle != null) {
                dispatch.setVehicle(bestVehicle);
                // vehicleId is a read-only field, managed by the relationship
                result.setVehicleId(bestVehicle.getId());
                result.setVehicleNumber(bestVehicle.getVehicleNumber());
            } else {
                result.setMessage("No suitable vehicle found");
                return result;
            }

            // ✅ FIX: Use WAITING_DRIVER_ACCEPTANCE instead of DRIVER_ASSIGNED
            dispatch.setStatus(DispatchStatus.WAITING_DRIVER_ACCEPTANCE);
            dispatch.setAssignedAt(java.time.LocalDateTime.now());

            result.setSuccess(true);
            result.setMessage("Driver and vehicle assigned successfully");

        } catch (Exception e) {
            log.error("Error in automatic assignment: {}", e.getMessage());
            result.setMessage(e.getMessage());
        }

        return result;
    }

    @Override
    public boolean validateDriverAssignment(Driver driver, String orderId) {
        if (driver == null) return false;
        if (!driver.getAvailable()) return false;
        return !dispatchRepository.existsByDriverIdAndStatusIn(
                driver.getId(),
                ACTIVE_DISPATCH_STATUSES);
    }

    @Override
    public boolean validateVehicleAssignment(Vehicle vehicle, String orderId) {
        if (vehicle == null) return false;
        if (!vehicle.isAvailable()) return false;
        return !dispatchRepository.existsByVehicleIdAndStatusIn(
                vehicle.getId(),
                ACTIVE_DISPATCH_STATUSES);
    }

    @Override
    public boolean isDriverAvailableForDispatch(String driverId, String orderId) {
        return driverRepository.findById(driverId)
                .map(driver -> validateDriverAssignment(driver, orderId))
                .orElse(false);
    }

    @Override
    public boolean isVehicleAvailableForDispatch(String vehicleId, String orderId) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> validateVehicleAssignment(vehicle, orderId))
                .orElse(false);
    }
}