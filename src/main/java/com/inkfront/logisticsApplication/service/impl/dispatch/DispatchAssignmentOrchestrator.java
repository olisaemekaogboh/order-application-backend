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
    private final OrderRepository orderRepository;
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

        log.info("Starting dispatch assignment orchestration for dispatch: {}", dispatch.getId());

        DispatchAssignmentResult result = DispatchAssignmentResult.builder()
                .success(false)
                .build();

        try {
            // 1. Validate and assign driver
            Driver driver = validateAndAssignDriver(driverId, dispatch.getOrder().getId());
            dispatch.setDriver(driver);

            // 2. Validate and assign vehicle
            Vehicle vehicle = validateAndAssignVehicle(vehicleId, dispatch.getOrder().getId());
            dispatch.setVehicle(vehicle);

            // 3. Update dispatch status
            dispatch.setStatus(DispatchStatus.WAITING_DRIVER_ACCEPTANCE);
            dispatch.setAssignedAt(LocalDateTime.now());
            if (notes != null) {
                dispatch.setNotes(notes);
            }

            // 4. Update driver availability
            driver.setAvailable(false);
            driverRepository.save(driver);

            // 5. Update vehicle status
            vehicle.setStatus(VehicleStatus.ASSIGNED);
            vehicleRepository.save(vehicle);

            // 6. Update order status
            Order order = dispatch.getOrder();
            order.setStatus(OrderStatus.DISPATCH);
            order.setDriver(driver);
            orderRepository.save(order);

            // 7. Save dispatch
            dispatch = dispatchRepository.save(dispatch);

            // 8. Log history
            logDispatchHistory(dispatch, DispatchStatus.PENDING, DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                    assignedBy, "Dispatch assigned to driver: " + driver.getName() +
                            ", vehicle: " + vehicle.getVehicleNumber());

            // 9. Send notifications
            notificationService.notifyDriverAssigned(dispatch, driver.getName());
            notificationService.notifyVehicleAssigned(dispatch, vehicle.getVehicleNumber());
            notificationService.notifyDispatchAssigned(dispatch);

            // 10. Publish events
            eventPublisher.publishDispatchAssigned(dispatch);

            // 11. Start tracking
            try {
                StartTrackingRequestDTO trackingRequest = StartTrackingRequestDTO.builder()
                        .orderId(order.getId())
                        .driverId(driver.getId())
                        .build();
                trackingService.startTracking(trackingRequest, assignedBy);
                log.info("Tracking started for order: {}", order.getId());
            } catch (Exception e) {
                log.warn("Could not start tracking for dispatch {}: {}", dispatch.getId(), e.getMessage());
            }

            // 12. Build success result
            result.setSuccess(true);
            result.setDriverId(driver.getId());
            result.setDriverName(driver.getName());
            result.setVehicleId(vehicle.getId());
            result.setVehicleNumber(vehicle.getVehicleNumber());
            result.setMessage("Dispatch assigned successfully");

            log.info("Dispatch assignment completed successfully: {}", dispatch.getId());

        } catch (Exception e) {
            log.error("Dispatch assignment failed: {}", e.getMessage(), e);
            result.setMessage(e.getMessage());

            if (dispatch.getId() != null) {
                logDispatchHistory(dispatch, dispatch.getStatus(), DispatchStatus.FAILED,
                        assignedBy, "Assignment failed: " + e.getMessage());
            }
        }

        return result;
    }

    private Driver validateAndAssignDriver(String driverId, String orderId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));

        if (!driver.getVerified()) {
            throw new DriverUnavailableException("Driver is not verified");
        }

        if (!driver.getAvailable()) {
            throw new DriverUnavailableException("Driver is not available");
        }

        boolean hasActiveDispatch = dispatchRepository.existsByDriverIdAndStatusIn(
                driverId,
                List.of(
                        DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                        DispatchStatus.DRIVER_ACCEPTED,
                        DispatchStatus.EN_ROUTE_PICKUP,
                        DispatchStatus.PICKUP_COMPLETED,
                        DispatchStatus.DELIVERY_IN_PROGRESS
                )
        );

        if (hasActiveDispatch) {
            throw new DriverUnavailableException("Driver already has an active dispatch");
        }

        return driver;
    }

    private Vehicle validateAndAssignVehicle(String vehicleId, String orderId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        if (!vehicle.isAvailable()) {
            throw new VehicleUnavailableException("Vehicle is not available");
        }

        if (vehicle.getStatus() == VehicleStatus.UNDER_MAINTENANCE) {
            throw new VehicleUnavailableException("Vehicle is under maintenance");
        }

        boolean hasActiveAssignment = dispatchRepository.existsByVehicleIdAndStatusIn(
                vehicleId,
                List.of(
                        DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                        DispatchStatus.DRIVER_ACCEPTED,
                        DispatchStatus.EN_ROUTE_PICKUP,
                        DispatchStatus.PICKUP_COMPLETED,
                        DispatchStatus.DELIVERY_IN_PROGRESS
                )
        );

        if (hasActiveAssignment) {
            throw new VehicleUnavailableException("Vehicle is already assigned to another dispatch");
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
    public void releaseResources(Dispatch dispatch, String userId, String reason) {
        log.info("Releasing resources for dispatch: {}", dispatch.getId());

        Driver driver = dispatch.getDriver();
        if (driver != null) {
            driver.setAvailable(true);
            driverRepository.save(driver);
        }

        Vehicle vehicle = dispatch.getVehicle();
        if (vehicle != null) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        Order order = dispatch.getOrder();
        if (dispatch.getStatus() == DispatchStatus.DELIVERED) {
            order.setStatus(OrderStatus.DELIVERED);  // ✅ Changed from COMPLETED to DELIVERED
            order.setDeliveryDate(LocalDateTime.now());
        } else if (dispatch.getStatus() == DispatchStatus.CANCELLED ||
                dispatch.getStatus() == DispatchStatus.FAILED) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            order.setCancellationReason(reason);
        }
        orderRepository.save(order);

        logDispatchHistory(dispatch, dispatch.getStatus(), dispatch.getStatus(),
                userId, "Resources released: " + reason);
    }
}