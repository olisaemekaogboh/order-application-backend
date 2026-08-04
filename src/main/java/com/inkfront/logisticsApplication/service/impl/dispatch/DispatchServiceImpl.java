package com.inkfront.logisticsApplication.service.impl.dispatch;

import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.domain.entity.dispatch.DispatchHistory;
import com.inkfront.logisticsApplication.domain.enums.DispatchStatus;
import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import com.inkfront.logisticsApplication.dto.request.dispatch.*;
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
import java.util.Optional;
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
    private final DispatchAssignmentOrchestrator assignmentOrchestrator;
    private final DispatchQueueService queueService;
    private final DispatchAnalyticsService analyticsService;
    private final DispatchEventPublisher eventPublisher;
    private final DispatchNotificationService notificationService;
    private final AuditService auditService;

    @Override
    public DispatchResponseDTO createDispatch(DispatchRequestDTO request, String userId) {
        log.info("Creating dispatch for order: {}", request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Validate order is ready for dispatch
        if (order.getStatus() != OrderStatus.READY_FOR_DISPATCH) {
            throw new DispatchStateException("Order must be in READY_FOR_DISPATCH status");
        }

        // Check if active dispatch already exists (ignore cancelled ones)
        Optional<Dispatch> existingDispatch = dispatchRepository.findByOrderId(order.getId());
        if (existingDispatch.isPresent()) {
            Dispatch dispatch = existingDispatch.get();
            // If cancelled, we can reuse it
            if (dispatch.getStatus() == DispatchStatus.CANCELLED) {
                log.info("Reusing cancelled dispatch: {}", dispatch.getId());
                // Reset the dispatch
                dispatch.setStatus(DispatchStatus.PENDING);
                dispatch.setDriver(null);
                dispatch.setVehicle(null);
                dispatch.setAssignedAt(null);
                dispatch.setAcceptedAt(null);
                dispatch.setCompletedAt(null);
                dispatch.setCancelledAt(null);
                dispatch.setFailureReason(null);
                dispatch.setPriority(request.getPriority() != null ? request.getPriority() : 0);
                dispatch.setScheduledTime(request.getScheduledTime());
                dispatch.setNotes(request.getNotes());
                dispatch.setVersion(dispatch.getVersion() + 1);

                dispatch = dispatchRepository.save(dispatch);

                logDispatchHistory(dispatch, DispatchStatus.CANCELLED, DispatchStatus.PENDING,
                        userId, "Reusing cancelled dispatch for new assignment");

                // Auto-assign if requested
                if (request.isAutoAssign()) {
                    DispatchAssignmentResult result = assignmentOrchestrator.assignDispatch(
                            dispatch, null, null, userId, request.getNotes());
                    if (result.isSuccess()) {
                        dispatch.setStatus(DispatchStatus.WAITING_DRIVER_ACCEPTANCE);
                        dispatch.setAssignedAt(LocalDateTime.now());
                        dispatch.setVersion(dispatch.getVersion() + 1);
                        dispatch = dispatchRepository.save(dispatch);
                        logDispatchHistory(dispatch, DispatchStatus.PENDING, DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                                userId, "Auto-assigned to driver and vehicle after reuse");
                    }
                }

                queueService.addToQueue(dispatch);
                return dispatchMapper.toResponseDTO(dispatch);
            }
            throw new DispatchStateException("Active dispatch already exists for this order");
        }

        // Create new dispatch as before
        Dispatch dispatch = new Dispatch();
        dispatch.setOrder(order);
        dispatch.setStatus(DispatchStatus.PENDING);
        dispatch.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        dispatch.setScheduledTime(request.getScheduledTime());
        dispatch.setNotes(request.getNotes());
        dispatch.setVersion(0L);

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, null, DispatchStatus.PENDING, userId, "Dispatch created");

        auditService.logAction(userId, "DISPATCH_CREATED", "Dispatch", dispatch.getId(),
                "Created dispatch for order " + order.getOrderNumber());

        notificationService.notifyDispatchCreated(dispatch);
        eventPublisher.publishDispatchCreated(dispatch);

        // Auto-assign if requested
        if (request.isAutoAssign()) {
            DispatchAssignmentResult result = assignmentOrchestrator.assignDispatch(
                    dispatch,
                    null,
                    null,
                    userId,
                    request.getNotes()
            );
            if (result.isSuccess()) {
                dispatch.setStatus(DispatchStatus.WAITING_DRIVER_ACCEPTANCE);
                dispatch.setAssignedAt(LocalDateTime.now());
                dispatch.setVersion(dispatch.getVersion() + 1);
                dispatch = dispatchRepository.save(dispatch);
                logDispatchHistory(dispatch, DispatchStatus.PENDING, DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                        userId, "Auto-assigned to driver and vehicle");
                return dispatchMapper.toResponseDTO(dispatch);
            }
        }

        // Add to queue for manual assignment
        queueService.addToQueue(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }
    @Override
    public DispatchResponseDTO manualAssignDispatch(ManualAssignDispatchRequestDTO request, String userId) {
        log.info("Manual assign dispatch for order: {}", request.getOrderId());

        // 1. Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // 2. Check if order is ready for dispatch
        if (order.getStatus() != OrderStatus.READY_FOR_DISPATCH) {
            throw new DispatchStateException("Order must be in READY_FOR_DISPATCH status");
        }

        // 3. Get existing dispatch or create new one
        Dispatch dispatch = dispatchRepository.findByOrderId(request.getOrderId()).orElse(null);

        if (dispatch == null) {
            // Create new dispatch
            log.info("Creating new dispatch for order: {}", request.getOrderId());
            dispatch = new Dispatch();
            dispatch.setOrder(order);
            dispatch.setStatus(DispatchStatus.PENDING);
            dispatch.setPriority(request.getPriority() != null ? request.getPriority() : 0);
            dispatch.setScheduledTime(request.getScheduledTime());
            dispatch.setNotes(request.getNotes());
            dispatch.setVersion(0L);
            dispatch = dispatchRepository.save(dispatch);

            logDispatchHistory(dispatch, null, DispatchStatus.PENDING, userId,
                    "Dispatch created for manual assignment");
        } else if (dispatch.getStatus() == DispatchStatus.CANCELLED) {
            // Reuse cancelled dispatch - reset it
            log.info("Reusing cancelled dispatch: {} for order: {}", dispatch.getId(), request.getOrderId());
            dispatch.setStatus(DispatchStatus.PENDING);
            dispatch.setPriority(request.getPriority() != null ? request.getPriority() : 0);
            dispatch.setScheduledTime(request.getScheduledTime());
            dispatch.setNotes(request.getNotes());
            dispatch.setDriver(null);
            dispatch.setVehicle(null);
            dispatch.setAssignedAt(null);
            dispatch.setAcceptedAt(null);
            dispatch.setCompletedAt(null);
            dispatch.setCancelledAt(null);
            dispatch.setRejectedAt(null);
            dispatch.setFailureReason(null);
            dispatch.setVersion(dispatch.getVersion() + 1);
            dispatch = dispatchRepository.save(dispatch);

            logDispatchHistory(dispatch, DispatchStatus.CANCELLED, DispatchStatus.PENDING, userId,
                    "Reusing cancelled dispatch for new assignment");
        } else if (dispatch.getStatus() != DispatchStatus.PENDING) {
            // Any other status - throw exception
            throw new DispatchStateException("Dispatch must be in PENDING status to assign. Current status: " + dispatch.getStatus());
        }

        // 4. Perform assignment with both driver and vehicle
        DispatchAssignmentResult result = assignmentOrchestrator.assignDispatch(
                dispatch,
                request.getDriverId(),
                request.getVehicleId(),
                userId,
                request.getNotes()
        );

        if (!result.isSuccess()) {
            throw new DispatchStateException("Assignment failed: " + result.getMessage());
        }

        // 5. Update dispatch status to WAITING_DRIVER_ACCEPTANCE
        dispatch.setStatus(DispatchStatus.WAITING_DRIVER_ACCEPTANCE);
        dispatch.setAssignedAt(LocalDateTime.now());
        dispatch.setVersion(dispatch.getVersion() + 1);
        dispatch = dispatchRepository.save(dispatch);

        // 6. Log history
        logDispatchHistory(dispatch, DispatchStatus.PENDING, DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                userId, "Dispatch manually assigned with driver: " + request.getDriverId() +
                        " and vehicle: " + request.getVehicleId());

        // 7. Update order status to DISPATCH
        order.setStatus(OrderStatus.DISPATCH);
        orderRepository.save(order);

        // 8. Audit
        auditService.logAction(userId, "DISPATCH_MANUALLY_ASSIGNED", "Dispatch", dispatch.getId(),
                "Manually assigned driver: " + request.getDriverId() + " and vehicle: " + request.getVehicleId());

        // 9. Notify
        notificationService.notifyDispatchAssigned(dispatch);
        eventPublisher.publishDispatchAssigned(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }
    @Override
    public DispatchResponseDTO assignDriver(String dispatchId, AssignDriverRequestDTO request, String userId) {
        log.info("Assigning driver to dispatch: {}", dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);

        // Validate state - allow PENDING only
        if (dispatch.getStatus() != DispatchStatus.PENDING) {
            throw new DispatchStateException("Dispatch must be in PENDING status to assign driver. Current: " + dispatch.getStatus());
        }

        // Assign driver only
        DispatchAssignmentResult result = assignmentOrchestrator.assignDispatch(
                dispatch,
                request.getDriverId(),
                dispatch.getVehicle() != null ? dispatch.getVehicle().getId() : null,
                userId,
                null
        );

        if (!result.isSuccess()) {
            throw new DispatchStateException("Driver assignment failed: " + result.getMessage());
        }

        // Update status to WAITING_DRIVER_ACCEPTANCE
        dispatch.setStatus(DispatchStatus.WAITING_DRIVER_ACCEPTANCE);
        dispatch.setAssignedAt(LocalDateTime.now());
        dispatch.setVersion(dispatch.getVersion() + 1);
        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, DispatchStatus.PENDING, DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                userId, "Driver assigned to dispatch");

        auditService.logAction(userId, "DRIVER_ASSIGNED_TO_DISPATCH", "Dispatch", dispatch.getId(),
                "Assigned driver: " + request.getDriverId());

        notificationService.notifyDispatchAssigned(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO assignVehicle(String dispatchId, AssignVehicleRequestDTO request, String userId) {
        log.info("Assigning vehicle to dispatch: {}", dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);

        // Validate state - allow PENDING only
        if (dispatch.getStatus() != DispatchStatus.PENDING) {
            throw new DispatchStateException("Dispatch must be in PENDING status to assign vehicle. Current: " + dispatch.getStatus());
        }

        // Assign vehicle only
        DispatchAssignmentResult result = assignmentOrchestrator.assignDispatch(
                dispatch,
                dispatch.getDriver() != null ? dispatch.getDriver().getId() : null,
                request.getVehicleId(),
                userId,
                null
        );

        if (!result.isSuccess()) {
            throw new DispatchStateException("Vehicle assignment failed: " + result.getMessage());
        }

        // Update status to WAITING_DRIVER_ACCEPTANCE
        dispatch.setStatus(DispatchStatus.WAITING_DRIVER_ACCEPTANCE);
        dispatch.setAssignedAt(LocalDateTime.now());
        dispatch.setVersion(dispatch.getVersion() + 1);
        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, DispatchStatus.PENDING, DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                userId, "Vehicle assigned to dispatch");

        auditService.logAction(userId, "VEHICLE_ASSIGNED_TO_DISPATCH", "Dispatch", dispatch.getId(),
                "Assigned vehicle: " + request.getVehicleId());

        notificationService.notifyDispatchAssigned(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO acceptDispatch(String dispatchId, String userId) {
        log.info("Accepting dispatch: {}", dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);
        validateTransition(dispatch.getStatus(), DispatchStatus.DRIVER_ACCEPTED);

        // Ensure dispatch has driver assigned
        if (dispatch.getDriver() == null) {
            throw new DispatchStateException("Cannot accept dispatch without an assigned driver");
        }

        DispatchStatus oldStatus = dispatch.getStatus();
        dispatch.setStatus(DispatchStatus.DRIVER_ACCEPTED);
        dispatch.setAcceptedAt(LocalDateTime.now());
        dispatch.setVersion(dispatch.getVersion() + 1);

        // Update order status
        Order order = dispatch.getOrder();
        order.setStatus(OrderStatus.DISPATCH);
        orderRepository.save(order);

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, oldStatus,
                DispatchStatus.DRIVER_ACCEPTED, userId, "Dispatch accepted by driver");

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
        validateTransition(dispatch.getStatus(), DispatchStatus.FAILED);

        DispatchStatus oldStatus = dispatch.getStatus();

        // Release resources
        assignmentOrchestrator.releaseResources(dispatch, userId, reason);

        dispatch.setStatus(DispatchStatus.FAILED);
        dispatch.setRejectedAt(LocalDateTime.now());
        dispatch.setFailureReason(reason);
        dispatch.setVersion(dispatch.getVersion() + 1);
        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, oldStatus,
                DispatchStatus.FAILED, userId, "Dispatch rejected: " + reason);

        auditService.logAction(userId, "DISPATCH_REJECTED", "Dispatch", dispatch.getId(),
                "Dispatch rejected: " + reason);

        notificationService.notifyDispatchRejected(dispatch, reason);
        eventPublisher.publishDispatchRejected(dispatch);

        // Handle retry
        if (dispatch.getRetryCount() < 3) {
            dispatch.setRetryCount(dispatch.getRetryCount() + 1);
            dispatch.setStatus(DispatchStatus.PENDING);
            dispatch.setDriver(null);
            dispatch.setVehicle(null);
            dispatch.setAssignedAt(null);
            dispatch.setVersion(dispatch.getVersion() + 1);
            dispatchRepository.save(dispatch);
            queueService.retryFailedDispatch(dispatchId);
            log.info("Retry scheduled for dispatch: {}", dispatchId);
        }

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO reassignDispatch(String dispatchId, String userId) {
        log.info("Reassigning dispatch: {}", dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);

        // Validate can reassign
        if (dispatch.getStatus() != DispatchStatus.FAILED &&
                dispatch.getStatus() != DispatchStatus.WAITING_DRIVER_ACCEPTANCE) {
            throw new DispatchStateException("Dispatch must be in FAILED or WAITING_DRIVER_ACCEPTANCE status to reassign");
        }

        DispatchStatus oldStatus = dispatch.getStatus();

        // Release current resources
        assignmentOrchestrator.releaseResources(dispatch, userId, "Reassignment");

        // Reset dispatch
        dispatch.setStatus(DispatchStatus.PENDING);
        dispatch.setDriver(null);
        dispatch.setVehicle(null);
        dispatch.setAssignedAt(null);
        dispatch.setAcceptedAt(null);
        dispatch.setRejectedAt(null);
        dispatch.setRetryCount(dispatch.getRetryCount() + 1);
        dispatch.setVersion(dispatch.getVersion() + 1);
        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, oldStatus, DispatchStatus.PENDING,
                userId, "Dispatch reassigned, retry count: " + dispatch.getRetryCount());

        auditService.logAction(userId, "DISPATCH_REASSIGNED", "Dispatch", dispatch.getId(),
                "Dispatch reassigned");

        // Add back to queue
        queueService.addToQueue(dispatch);

        notificationService.notifyDispatchAssigned(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO cancelDispatch(String dispatchId, String reason, String userId) {
        log.info("Cancelling dispatch: {} reason: {}", dispatchId, reason);

        Dispatch dispatch = findDispatch(dispatchId);
        validateTransition(dispatch.getStatus(), DispatchStatus.CANCELLED);

        DispatchStatus oldStatus = dispatch.getStatus();

        // Release resources
        assignmentOrchestrator.releaseResources(dispatch, userId, reason);

        dispatch.setStatus(DispatchStatus.CANCELLED);
        dispatch.setCancelledAt(LocalDateTime.now());
        dispatch.setFailureReason(reason);
        dispatch.setVersion(dispatch.getVersion() + 1);
        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(dispatch, oldStatus, DispatchStatus.CANCELLED,
                userId, "Dispatch cancelled: " + reason);

        auditService.logAction(userId, "DISPATCH_CANCELLED", "Dispatch", dispatch.getId(),
                "Dispatch cancelled: " + reason);

        notificationService.notifyDispatchCancelled(dispatch, reason);
        eventPublisher.publishDispatchCancelled(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    @Transactional
    public DispatchResponseDTO completeDispatch(String dispatchId, String userId) {
        log.info("Completing dispatch: {}", dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);

        // Check if already completed
        if (dispatch.getStatus() == DispatchStatus.DELIVERED) {
            throw new DispatchStateException("Dispatch already completed");
        }

        validateTransition(dispatch.getStatus(), DispatchStatus.DELIVERED);

        DispatchStatus oldStatus = dispatch.getStatus();

        // Update dispatch
        dispatch.setStatus(DispatchStatus.DELIVERED);
        dispatch.setCompletedAt(LocalDateTime.now());
        dispatch.setVersion(dispatch.getVersion() + 1);
        dispatch = dispatchRepository.save(dispatch);

        // Release resources - wrap in try-catch to prevent rollback
        try {
            assignmentOrchestrator.releaseResources(dispatch, userId, "Delivery completed");
        } catch (Exception e) {
            log.error("Error releasing resources: {}", e.getMessage());
        }

        // Log history
        try {
            logDispatchHistory(dispatch, oldStatus, DispatchStatus.DELIVERED,
                    userId, "Dispatch completed");
        } catch (Exception e) {
            log.error("Error logging history: {}", e.getMessage());
        }

        // Audit
        try {
            auditService.logAction(userId, "DISPATCH_COMPLETED", "Dispatch", dispatch.getId(),
                    "Dispatch completed");
        } catch (Exception e) {
            log.error("Error logging audit: {}", e.getMessage());
        }

        // Notifications - wrapped in try-catch to prevent rollback
        try {
            notificationService.notifyDispatchCompleted(dispatch);
        } catch (Exception e) {
            log.error("Failed to send completion notifications: {}", e.getMessage());
        }

        try {
            eventPublisher.publishDispatchCompleted(dispatch);
        } catch (Exception e) {
            log.error("Failed to publish completion event: {}", e.getMessage());
        }

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
        return analyticsService.getAnalytics();
    }

    // ------------------- Private Helpers -------------------

    private Dispatch findDispatch(String dispatchId) {
        return dispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new DispatchNotFoundException("Dispatch not found: " + dispatchId));
    }

    private void validateTransition(DispatchStatus currentStatus, DispatchStatus newStatus) {
        if (!stateValidator.isValidTransition(currentStatus, newStatus)) {
            throw new DispatchStateException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
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
        history.setVersion(0L);
        historyRepository.save(history);
    }

    private PaginatedResponseDTO<DispatchSummaryDTO> toPaginatedResponse(Page<Dispatch> page) {
        List<DispatchSummaryDTO> content = page.getContent().stream()
                .map(dispatchMapper::toSummaryDTO)
                .collect(Collectors.toList());
        return new PaginatedResponseDTO<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}