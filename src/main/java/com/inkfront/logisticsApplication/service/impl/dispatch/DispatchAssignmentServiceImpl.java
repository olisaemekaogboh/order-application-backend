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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public List<Driver> findAvailableDriversForDispatch(String orderId) {

        return driverRepository.findByAvailableTrueAndVerifiedTrue()
                .stream()
                .filter(driver ->
                        !dispatchRepository.existsByDriverIdAndStatusIn(
                                driver.getId(),
                                ACTIVE_DISPATCH_STATUSES
                        ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findAvailableVehiclesForDispatch(String orderId) {

        return vehicleRepository.findByStatusAndDeletedFalse(
                        VehicleStatus.AVAILABLE)
                .stream()
                .filter(vehicle ->
                        !dispatchRepository.existsByVehicleIdAndStatusIn(
                                vehicle.getId(),
                                ACTIVE_DISPATCH_STATUSES
                        ))
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public Driver assignBestDriver(String orderId) {

        List<Driver> drivers =
                findAvailableDriversForDispatch(orderId);

        if (drivers.isEmpty()) {
            throw new DriverUnavailableException(
                    "No available drivers found."
            );
        }

        return drivers.stream()
                .sorted(
                        Comparator
                                .comparingDouble(Driver::getRating)
                                .reversed()
                                .thenComparingInt(Driver::getCompletedOrders)
                )
                .findFirst()
                .orElseThrow();
    }
    @Override
    @Transactional(readOnly = true)
    public Vehicle assignBestVehicle(String orderId) {

        List<Vehicle> vehicles =
                findAvailableVehiclesForDispatch(orderId);

        if (vehicles.isEmpty()) {
            throw new VehicleUnavailableException(
                    "No available vehicles found."
            );
        }

        return vehicles.stream()
                .min(
                        Comparator.comparingDouble(
                                Vehicle::getCurrentMileage
                        )
                )
                .orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public DispatchAssignmentResult assignBestDriverAndVehicle(
            Dispatch dispatch) {

        Driver driver =
                assignBestDriver(dispatch.getOrder().getId());

        Vehicle vehicle =
                assignBestVehicle(dispatch.getOrder().getId());

        return DispatchAssignmentResult.builder()
                .success(true)
                .driverId(driver.getId())
                .driverName(driver.getUser().getFullName())
                .vehicleId(vehicle.getId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .message("Best resources found.")
                .build();
    }

    @Override
    public boolean validateDriverAssignment(
            Driver driver,
            String orderId) {

        return driver != null
                && Boolean.TRUE.equals(driver.getAvailable())
                && !dispatchRepository.existsByDriverIdAndStatusIn(
                driver.getId(),
                ACTIVE_DISPATCH_STATUSES
        );
    }

    @Override
    public boolean validateVehicleAssignment(
            Vehicle vehicle,
            String orderId) {

        return vehicle != null
                && vehicle.isAvailable()
                && !dispatchRepository.existsByVehicleIdAndStatusIn(
                vehicle.getId(),
                ACTIVE_DISPATCH_STATUSES
        );
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