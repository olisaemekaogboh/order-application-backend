package com.inkfront.logisticsApplication.service.impl.dispatch;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.domain.entity.dispatch.DispatchHistory;
import com.inkfront.logisticsApplication.domain.entity.vehicle.Vehicle;
import com.inkfront.logisticsApplication.domain.enums.DispatchStatus;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.dto.request.dispatch.*;
import com.inkfront.logisticsApplication.dto.request.tracking.StartTrackingRequestDTO;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.dispatch.*;
import com.inkfront.logisticsApplication.events.publisher.DispatchEventPublisher;
import com.inkfront.logisticsApplication.exception.dispatch.DispatchNotFoundException;
import com.inkfront.logisticsApplication.exception.dispatch.DispatchStateException;
import com.inkfront.logisticsApplication.mapper.dispatch.DispatchMapper;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.dispatch.DispatchHistoryRepository;
import com.inkfront.logisticsApplication.repository.dispatch.DispatchRepository;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import com.inkfront.logisticsApplication.service.interfaces.dispatch.*;
import com.inkfront.logisticsApplication.service.interfaces.tracking.TrackingService;
import com.inkfront.logisticsApplication.validator.dispatch.DispatchStateValidator;
import com.inkfront.logisticsApplication.validator.dispatch.DispatchValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DispatchServiceImpl implements DispatchService {

    private final DispatchRepository dispatchRepository;
    private final DispatchHistoryRepository historyRepository;
    private final OrderRepository orderRepository;
    private final DispatchMapper dispatchMapper;
    private final DispatchValidator dispatchValidator;
    private final DispatchStateValidator stateValidator;
    private final DispatchAssignmentService assignmentService;
    private final DispatchQueueService queueService;
    private final DispatchEventPublisher eventPublisher;
    private final DispatchNotificationService notificationService;
    private final AuditService auditService;
    private final TrackingService trackingService;

    @Override
    public DispatchResponseDTO createDispatch(DispatchRequestDTO request, String userId) {
        log.info("Creating dispatch for order: {}", request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        dispatchValidator.validateOrderNotDispatched(order.getId());

        Dispatch dispatch = new Dispatch();
        dispatch.setOrder(order);
        dispatch.setStatus(DispatchStatus.PENDING);
        dispatch.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        dispatch.setScheduledTime(request.getScheduledTime());
        dispatch.setNotes(request.getNotes());

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, null, DispatchStatus.PENDING, userId, "Dispatch created");

        auditService.logAction(userId, "DISPATCH_CREATED", "Dispatch", dispatch.getId(),
                "Created dispatch for order " + order.getOrderNumber());

        notificationService.notifyDispatchCreated(dispatch);
        eventPublisher.publishDispatchCreated(dispatch);

        if (request.isAutoAssign()) {
            assignBestDriverAndVehicle(dispatch);
        }

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO assignDriver(String dispatchId, AssignDriverRequestDTO request, String userId) {
        log.info("Assigning driver to dispatch: {}", dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);
        stateValidator.validateTransition(dispatch.getStatus(), DispatchStatus.DRIVER_ASSIGNED);

        Driver driver = assignmentService.findAvailableDriversForDispatch(dispatch.getOrder().getId())
                .stream().filter(d -> d.getId().equals(request.getDriverId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Driver not available"));

        dispatch.setDriver(driver);   // relationship handles the foreign key
        // No need to set driverId – it will be populated automatically
        dispatch.setStatus(DispatchStatus.DRIVER_ASSIGNED);
        dispatch.setAssignedAt(LocalDateTime.now());

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, DispatchStatus.DRIVER_ASSIGNED, DispatchStatus.DRIVER_ASSIGNED, userId,
                "Driver assigned: " + driver.getName());

        auditService.logAction(userId, "DISPATCH_DRIVER_ASSIGNED", "Dispatch", dispatch.getId(),
                "Assigned driver " + driver.getName() + " to dispatch");

        notificationService.notifyDriverAssigned(dispatch, driver.getName());
        eventPublisher.publishDispatchAssigned(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO assignVehicle(String dispatchId, AssignVehicleRequestDTO request, String userId) {
        log.info("Assigning vehicle to dispatch: {}", dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);
        stateValidator.validateTransition(dispatch.getStatus(), DispatchStatus.VEHICLE_ASSIGNED);

        Vehicle vehicle = assignmentService.findAvailableVehiclesForDispatch(dispatch.getOrder().getId())
                .stream().filter(v -> v.getId().equals(request.getVehicleId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not available"));

        dispatch.setVehicle(vehicle);   // relationship handles the foreign key
        // No need to set vehicleId – it will be populated automatically
        dispatch.setStatus(DispatchStatus.VEHICLE_ASSIGNED);
        dispatch.setAssignedAt(LocalDateTime.now());

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, DispatchStatus.VEHICLE_ASSIGNED, DispatchStatus.VEHICLE_ASSIGNED, userId,
                "Vehicle assigned: " + vehicle.getVehicleNumber());

        auditService.logAction(userId, "DISPATCH_VEHICLE_ASSIGNED", "Dispatch", dispatch.getId(),
                "Assigned vehicle " + vehicle.getVehicleNumber() + " to dispatch");

        notificationService.notifyVehicleAssigned(dispatch, vehicle.getVehicleNumber());
        eventPublisher.publishDispatchAssigned(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }
    @Override
    public DispatchResponseDTO acceptDispatch(String dispatchId, String userId) {
        log.info("Accepting dispatch: {}", dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);
        stateValidator.validateTransition(dispatch.getStatus(), DispatchStatus.DRIVER_ACCEPTED);

        dispatch.setStatus(DispatchStatus.DRIVER_ACCEPTED);
        dispatch.setAcceptedAt(LocalDateTime.now());

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, DispatchStatus.DRIVER_ACCEPTED, DispatchStatus.DRIVER_ACCEPTED, userId,
                "Dispatch accepted");

        auditService.logAction(userId, "DISPATCH_ACCEPTED", "Dispatch", dispatch.getId(),
                "Dispatch accepted");

        notificationService.notifyDispatchAccepted(dispatch);
        eventPublisher.publishDispatchAccepted(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO rejectDispatch(String dispatchId, String reason, String userId) {
        log.info("Rejecting dispatch: {} reason: {}", dispatchId, reason);

        Dispatch dispatch = findDispatch(dispatchId);
        stateValidator.validateTransition(dispatch.getStatus(), DispatchStatus.DRIVER_REJECTED);

        dispatch.setStatus(DispatchStatus.DRIVER_REJECTED);
        dispatch.setRejectedAt(LocalDateTime.now());
        dispatch.setFailureReason(reason);

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, DispatchStatus.DRIVER_REJECTED, DispatchStatus.DRIVER_REJECTED, userId,
                "Dispatch rejected: " + reason);

        auditService.logAction(userId, "DISPATCH_REJECTED", "Dispatch", dispatch.getId(),
                "Dispatch rejected: " + reason);

        notificationService.notifyDispatchRejected(dispatch, reason);
        eventPublisher.publishDispatchRejected(dispatch);

        if (dispatch.getRetryCount() < 3) {
            dispatch.setRetryCount(dispatch.getRetryCount() + 1);
            dispatch.setStatus(DispatchStatus.PENDING);
            dispatchRepository.save(dispatch);
            queueService.retryFailedDispatch(dispatchId);
        }

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO reassignDispatch(String dispatchId, String userId) {
        log.info("Reassigning dispatch: {}", dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);
        stateValidator.validateTransition(dispatch.getStatus(), DispatchStatus.REASSIGNED);

        dispatch.setStatus(DispatchStatus.REASSIGNED);
        dispatch.setRetryCount(dispatch.getRetryCount() + 1);

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, DispatchStatus.REASSIGNED, DispatchStatus.REASSIGNED, userId,
                "Dispatch reassigned");

        auditService.logAction(userId, "DISPATCH_REASSIGNED", "Dispatch", dispatch.getId(),
                "Dispatch reassigned");

        dispatch.setStatus(DispatchStatus.PENDING);
        dispatch.setDriver(null);
        dispatch.setVehicle(null);
        // driverId and vehicleId will be null because they are read‑only and derived from the relationships
        dispatch.setAssignedAt(null);
        dispatchRepository.save(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }
    @Override
    public DispatchResponseDTO cancelDispatch(String dispatchId, String reason, String userId) {
        log.info("Cancelling dispatch: {} reason: {}", dispatchId, reason);

        Dispatch dispatch = findDispatch(dispatchId);
        stateValidator.validateTransition(dispatch.getStatus(), DispatchStatus.CANCELLED);

        dispatch.setStatus(DispatchStatus.CANCELLED);
        dispatch.setCancelledAt(LocalDateTime.now());
        dispatch.setFailureReason(reason);

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, DispatchStatus.CANCELLED, DispatchStatus.CANCELLED, userId,
                "Dispatch cancelled: " + reason);

        auditService.logAction(userId, "DISPATCH_CANCELLED", "Dispatch", dispatch.getId(),
                "Dispatch cancelled: " + reason);

        notificationService.notifyDispatchCancelled(dispatch, reason);
        eventPublisher.publishDispatchCancelled(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO completeDispatch(String dispatchId, String userId) {
        log.info("Completing dispatch: {}", dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);
        stateValidator.validateTransition(dispatch.getStatus(), DispatchStatus.DELIVERED);

        dispatch.setStatus(DispatchStatus.DELIVERED);
        dispatch.setCompletedAt(LocalDateTime.now());

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, DispatchStatus.DELIVERED, DispatchStatus.DELIVERED, userId,
                "Dispatch completed");

        auditService.logAction(userId, "DISPATCH_COMPLETED", "Dispatch", dispatch.getId(),
                "Dispatch completed");

        notificationService.notifyDispatchCompleted(dispatch);
        eventPublisher.publishDispatchCompleted(dispatch);

        // Update order status
        Order order = dispatch.getOrder();
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveryDate(LocalDateTime.now());
        orderRepository.save(order);

        // Tracking will be completed automatically when order status changes to DELIVERED
        // The tracking module listens to order status changes via events

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO getDispatchById(String dispatchId) {
        Dispatch dispatch = findDispatch(dispatchId);
        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO getDispatchByOrder(String orderId) {
        Dispatch dispatch = dispatchRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DispatchNotFoundException("No dispatch found for order: " + orderId));
        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public PaginatedResponseDTO<DispatchSummaryDTO> getDispatchesByDriver(String driverId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Dispatch> pageResult = dispatchRepository.findByDriverId(driverId, pageable);
        return toPaginatedResponse(pageResult);
    }

    @Override
    public PaginatedResponseDTO<DispatchSummaryDTO> getDispatchesByVehicle(String vehicleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Dispatch> pageResult = dispatchRepository.findByVehicleId(vehicleId, pageable);
        return toPaginatedResponse(pageResult);
    }

    @Override
    public PaginatedResponseDTO<DispatchSummaryDTO> getAllDispatches(int page, int size, String status, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Dispatch> pageResult;
        if (status != null && !status.isEmpty()) {
            pageResult = dispatchRepository.findByStatus(DispatchStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            pageResult = dispatchRepository.findAll(pageable);
        }
        return toPaginatedResponse(pageResult);
    }

    @Override
    public DispatchAnalyticsDTO getDispatchAnalytics() {
        // delegate to analytics service
        return new DispatchAnalyticsDTO();
    }

    // ------------------- Private Helpers -------------------

    private Dispatch findDispatch(String dispatchId) {
        return dispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new DispatchNotFoundException("Dispatch not found: " + dispatchId));
    }

    private void logDispatchHistory(Dispatch dispatch, DispatchStatus oldStatus, DispatchStatus newStatus,
                                    String userId, String reason) {
        DispatchHistory history = new DispatchHistory();
        history.setDispatch(dispatch);
        history.setPreviousStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(userId);
        history.setReason(reason);
        historyRepository.save(history);
    }

    private void assignBestDriverAndVehicle(Dispatch dispatch) {
        DispatchAssignmentResult result = assignmentService.assignBestDriverAndVehicle(dispatch);
        if (result.isSuccess()) {
            // The assignment service already sets the driver and vehicle relationships
            dispatchRepository.save(dispatch);
            notificationService.notifyDriverAssigned(dispatch, result.getDriverName());
            notificationService.notifyVehicleAssigned(dispatch, result.getVehicleNumber());
            try {
                // Start tracking
                StartTrackingRequestDTO trackingRequest = StartTrackingRequestDTO.builder()
                        .orderId(dispatch.getOrder().getId())
                        .driverId(result.getDriverId())
                        .build();
                trackingService.startTracking(trackingRequest, "SYSTEM");
            } catch (Exception e) {
                log.warn("Could not start tracking for dispatch {}: {}", dispatch.getId(), e.getMessage());
            }
            eventPublisher.publishDispatchAssigned(dispatch);
        } else {
            log.warn("Automatic assignment failed for dispatch {}: {}", dispatch.getId(), result.getMessage());
            dispatch.setStatus(DispatchStatus.FAILED);
            dispatch.setFailureReason(result.getMessage());
            dispatchRepository.save(dispatch);
            eventPublisher.publishDispatchRejected(dispatch);
        }
    }

    private PaginatedResponseDTO<DispatchSummaryDTO> toPaginatedResponse(Page<Dispatch> page) {
        List<DispatchSummaryDTO> content = page.getContent().stream()
                .map(dispatchMapper::toSummaryDTO)
                .collect(Collectors.toList());
        return new PaginatedResponseDTO<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}