package com.inkfront.logisticsApplication.service.impl.dispatch;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.domain.entity.dispatch.DispatchHistory;
import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.enums.DispatchStatus;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.dto.request.tracking.StartTrackingRequestDTO;
import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchAssignmentResult;
import com.inkfront.logisticsApplication.events.publisher.DispatchEventPublisher;
import com.inkfront.logisticsApplication.exception.dispatch.DriverUnavailableException;
import com.inkfront.logisticsApplication.exception.dispatch.VehicleUnavailableException;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.dispatch.DispatchHistoryRepository;
import com.inkfront.logisticsApplication.repository.dispatch.DispatchRepository;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import com.inkfront.logisticsApplication.service.interfaces.dispatch.DispatchNotificationService;
import com.inkfront.logisticsApplication.service.interfaces.tracking.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchAssignmentOrchestrator {

    private final DispatchRepository dispatchRepository;
    private final DispatchHistoryRepository historyRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DispatchNotificationService notificationService;
    private final DispatchEventPublisher eventPublisher;
    private final TrackingService trackingService;

    @Transactional
    public DispatchAssignmentResult assignDispatch(
            Dispatch dispatch,
            String driverId,
            String vehicleId,
            String assignedBy,
            String notes) {

        log.info("Assigning dispatch {}", dispatch.getId());

        Driver driver = validateAndAssignDriver(driverId);

        Vehicle vehicle = validateAndAssignVehicle(vehicleId);

        dispatch.setDriver(driver);
        dispatch.setVehicle(vehicle);

        dispatch.setStatus(DispatchStatus.WAITING_DRIVER_ACCEPTANCE);
        dispatch.setAssignedAt(LocalDateTime.now());

        if (notes != null && !notes.isBlank()) {
            dispatch.setNotes(notes);
        }

        driver.setAvailable(false);
        driver.setLastActive(LocalDateTime.now());

        vehicle.assign();

        driverRepository.save(driver);
        vehicleRepository.save(vehicle);

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(
                dispatch,
                DispatchStatus.PENDING,
                DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                assignedBy,
                "Driver assigned: "
                        + driver.getUser().getFullName()
                        + ", Vehicle: "
                        + vehicle.getVehicleNumber()
        );

        notificationService.notifyDriverAssigned(
                dispatch,
                driver.getUser().getFullName()
        );

        notificationService.notifyVehicleAssigned(
                dispatch,
                vehicle.getVehicleNumber()
        );

        notificationService.notifyDispatchAssigned(dispatch);

        eventPublisher.publishDispatchAssigned(dispatch);

        try {

            StartTrackingRequestDTO trackingRequest =
                    StartTrackingRequestDTO.builder()
                            .orderId(dispatch.getOrder().getId())
                            .driverId(driver.getId())
                            .build();



        } catch (Exception e) {

            log.error(
                    "Unable to initialize tracking for dispatch {}",
                    dispatch.getId(),
                    e
            );

            /*
             * Tracking should not rollback assignment.
             * Assignment has already succeeded.
             */
        }

        return DispatchAssignmentResult.builder()
                .success(true)
                .driverId(driver.getId())
                .driverName(driver.getUser().getFullName())
                .vehicleId(vehicle.getId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .message("Dispatch assigned successfully")
                .build();
    }
    private Driver validateAndAssignDriver(String driverId) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new DriverUnavailableException(
                                "Driver not found: " + driverId
                        ));

        if (!Boolean.TRUE.equals(driver.getVerified())) {
            throw new DriverUnavailableException(
                    "Driver is not verified."
            );
        }

        if (!Boolean.TRUE.equals(driver.getAvailable())) {
            throw new DriverUnavailableException(
                    "Driver is currently unavailable."
            );
        }

        List<DispatchStatus> activeStatuses = List.of(
                DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                DispatchStatus.DRIVER_ACCEPTED,
                DispatchStatus.EN_ROUTE_PICKUP,
                DispatchStatus.PICKUP_COMPLETED,
                DispatchStatus.DELIVERY_IN_PROGRESS
        );

        boolean alreadyAssigned = dispatchRepository.existsByDriverIdAndStatusIn(
                driverId,
                activeStatuses
        );

        if (alreadyAssigned) {
            throw new DriverUnavailableException(
                    "Driver already has an active dispatch."
            );
        }

        return driver;
    }

    private Vehicle validateAndAssignVehicle(String vehicleId) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new VehicleUnavailableException(
                                "Vehicle not found: " + vehicleId
                        ));

        if (!vehicle.isAvailable()) {
            throw new VehicleUnavailableException(
                    "Vehicle is currently unavailable."
            );
        }

        if (vehicle.getStatus() == VehicleStatus.UNDER_MAINTENANCE) {
            throw new VehicleUnavailableException(
                    "Vehicle is under maintenance."
            );
        }

        List<DispatchStatus> activeStatuses = List.of(
                DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                DispatchStatus.DRIVER_ACCEPTED,
                DispatchStatus.EN_ROUTE_PICKUP,
                DispatchStatus.PICKUP_COMPLETED,
                DispatchStatus.DELIVERY_IN_PROGRESS
        );

        boolean alreadyAssigned = dispatchRepository.existsByVehicleIdAndStatusIn(
                vehicleId,
                activeStatuses
        );

        if (alreadyAssigned) {
            throw new VehicleUnavailableException(
                    "Vehicle already has an active dispatch."
            );
        }

        return vehicle;
    }

    private void logDispatchHistory(Dispatch dispatch, DispatchStatus oldStatus,
                                    DispatchStatus newStatus, String userId, String reason) {
        DispatchHistory history = new DispatchHistory();
        history.setDispatch(dispatch);
        history.setPreviousStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(userId);
        history.setReason(reason);
        historyRepository.save(history);
    }
    @Transactional
    public void releaseResources(
            Dispatch dispatch,
            String userId,
            String reason) {

        log.info("Releasing resources for dispatch {}", dispatch.getId());

        Driver driver = dispatch.getDriver();

        if (driver != null) {

            driver.setAvailable(true);
            driver.setLastActive(LocalDateTime.now());

            driverRepository.save(driver);
        }

        Vehicle vehicle = dispatch.getVehicle();

        if (vehicle != null) {

            vehicle.release();

            vehicleRepository.save(vehicle);
        }

        /*
         * Do NOT update the Order here.
         *
         * Order status is managed by:
         *
         * completeDispatch()
         * cancelDispatch()
         * rejectDispatch()
         *
         * This method ONLY releases resources.
         */

        logDispatchHistory(
                dispatch,
                dispatch.getStatus(),
                dispatch.getStatus(),
                userId,
                "Resources released. Reason: " + reason
        );
    }


}