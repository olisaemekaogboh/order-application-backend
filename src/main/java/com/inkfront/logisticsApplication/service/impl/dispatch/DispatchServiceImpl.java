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
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.exception.dispatch.DispatchNotFoundException;
import com.inkfront.logisticsApplication.exception.dispatch.DispatchStateException;
import com.inkfront.logisticsApplication.mapper.dispatch.DispatchMapper;
import com.inkfront.logisticsApplication.repository.DriverRepository;
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
    private final TrackingService trackingService;
    private final DriverRepository driverRepository;

    private static final int MAX_RETRIES = 3;

    @Override
    @Transactional
    public DispatchResponseDTO createDispatch(DispatchRequestDTO request, String userId) {

        log.info("Creating dispatch for order: {}", request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.READY_FOR_DISPATCH) {
            throw new DispatchStateException(
                    "Order must be in READY_FOR_DISPATCH status."
            );
        }

        Optional<Dispatch> existingDispatch =
                dispatchRepository.findByOrderId(order.getId());

        Dispatch dispatch;

        if (existingDispatch.isPresent()) {

            dispatch = existingDispatch.get();

            if (dispatch.getStatus() != DispatchStatus.CANCELLED) {
                throw new DispatchStateException(
                        "An active dispatch already exists for this order."
                );
            }

            log.info("Reusing cancelled dispatch {}", dispatch.getId());

            DispatchStatus previousStatus = dispatch.getStatus();

            dispatch.setStatus(DispatchStatus.PENDING);
            dispatch.setDriver(null);
            dispatch.setVehicle(null);

            dispatch.setAssignedAt(null);
            dispatch.setAcceptedAt(null);
            dispatch.setCompletedAt(null);
            dispatch.setCancelledAt(null);

            dispatch.setFailureReason(null);

            dispatch.setPriority(
                    request.getPriority() == null ? 0 : request.getPriority()
            );

            dispatch.setScheduledTime(request.getScheduledTime());
            dispatch.setNotes(request.getNotes());

            dispatch = dispatchRepository.save(dispatch);

            logDispatchHistory(
                    dispatch,
                    previousStatus,
                    DispatchStatus.PENDING,
                    userId,
                    "Cancelled dispatch reused"
            );

        } else {

            dispatch = new Dispatch();

            dispatch.setOrder(order);
            dispatch.setStatus(DispatchStatus.PENDING);

            dispatch.setPriority(
                    request.getPriority() == null ? 0 : request.getPriority()
            );

            dispatch.setScheduledTime(request.getScheduledTime());
            dispatch.setNotes(request.getNotes());

            dispatch = dispatchRepository.save(dispatch);

            logDispatchHistory(
                    dispatch,
                    null,
                    DispatchStatus.PENDING,
                    userId,
                    "Dispatch created"
            );

        }

        auditService.logAction(
                userId,
                "DISPATCH_CREATED",
                "Dispatch",
                dispatch.getId(),
                "Created dispatch for order " + order.getOrderNumber()
        );

        notificationService.notifyDispatchCreated(dispatch);

        eventPublisher.publishDispatchCreated(dispatch);

        /*
         * Do NOT assign driver or vehicle here.
         * Only enqueue for later processing.
         */

        if (request.isAutoAssign()) {
            queueService.addToQueue(dispatch);
            log.info("Dispatch {} queued for automatic assignment.", dispatch.getId());
        }

        return dispatchMapper.toResponseDTO(dispatch);
    }


    @Override
    @Transactional
    public DispatchResponseDTO manualAssignDispatch(
            ManualAssignDispatchRequestDTO request,
            String userId) {

        log.info("Manual assignment requested for order {}", request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.READY_FOR_DISPATCH) {
            throw new DispatchStateException(
                    "Order must be in READY_FOR_DISPATCH status."
            );
        }

        Dispatch dispatch;

        Optional<Dispatch> existing =
                dispatchRepository.findByOrderId(order.getId());

        if (existing.isPresent()) {

            dispatch = existing.get();

            if (dispatch.getStatus() == DispatchStatus.CANCELLED) {

                DispatchStatus previous = dispatch.getStatus();

                dispatch.setStatus(DispatchStatus.PENDING);
                dispatch.setDriver(null);
                dispatch.setVehicle(null);

                dispatch.setAssignedAt(null);
                dispatch.setAcceptedAt(null);
                dispatch.setCompletedAt(null);
                dispatch.setCancelledAt(null);
                dispatch.setRejectedAt(null);

                dispatch.setFailureReason(null);

                dispatch.setPriority(
                        request.getPriority() == null ? 0 : request.getPriority());

                dispatch.setScheduledTime(request.getScheduledTime());
                dispatch.setNotes(request.getNotes());

                dispatch = dispatchRepository.save(dispatch);

                logDispatchHistory(
                        dispatch,
                        previous,
                        DispatchStatus.PENDING,
                        userId,
                        "Cancelled dispatch reused");

            } else if (dispatch.getStatus() != DispatchStatus.PENDING) {

                throw new DispatchStateException(
                        "Dispatch is not available for assignment. Current status: "
                                + dispatch.getStatus());

            }

        } else {

            dispatch = new Dispatch();

            dispatch.setOrder(order);
            dispatch.setStatus(DispatchStatus.PENDING);

            dispatch.setPriority(
                    request.getPriority() == null ? 0 : request.getPriority());

            dispatch.setScheduledTime(request.getScheduledTime());
            dispatch.setNotes(request.getNotes());

            dispatch = dispatchRepository.save(dispatch);

            logDispatchHistory(
                    dispatch,
                    null,
                    DispatchStatus.PENDING,
                    userId,
                    "Dispatch created");

        }

        DispatchAssignmentResult result =
                assignmentOrchestrator.assignDispatch(
                        dispatch,
                        request.getDriverId(),
                        request.getVehicleId(),
                        userId,
                        request.getNotes());

        if (!result.isSuccess()) {

            dispatch.setStatus(DispatchStatus.FAILED);

            dispatch = dispatchRepository.save(dispatch);

            logDispatchHistory(
                    dispatch,
                    DispatchStatus.PENDING,
                    DispatchStatus.FAILED,
                    userId,
                    result.getMessage());

            throw new DispatchStateException(result.getMessage());
        }

        dispatch.setStatus(DispatchStatus.WAITING_DRIVER_ACCEPTANCE);
        dispatch.setAssignedAt(LocalDateTime.now());

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(
                dispatch,
                DispatchStatus.PENDING,
                DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                userId,
                "Driver and vehicle assigned manually");

        /*
         * IMPORTANT:
         * DO NOT update the Order status here.
         *
         * The order should remain READY_FOR_DISPATCH until the
         * driver accepts the dispatch.
         */

        auditService.logAction(
                userId,
                "DISPATCH_MANUAL_ASSIGNMENT",
                "Dispatch",
                dispatch.getId(),
                "Driver: " + request.getDriverId()
                        + ", Vehicle: " + request.getVehicleId());

        return dispatchMapper.toResponseDTO(dispatch);
    }


    @Override
    public DispatchResponseDTO assignDriver(String dispatchId,
                                            AssignDriverRequestDTO request,
                                            String userId) {

        log.info("Assigning driver {} to dispatch {}", request.getDriverId(), dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);

        if (dispatch.getStatus() != DispatchStatus.PENDING)
               {
            throw new DispatchStateException(
                    "Driver can only be assigned while dispatch is PENDING or VEHICLE_ASSIGNED");
        }




        DispatchStatus previous = dispatch.getStatus();



        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(
                dispatch,
                previous,
                dispatch.getStatus(),
                userId,
                "Driver assigned");

        notificationService.notifyDispatchAssigned(dispatch);

        auditService.logAction(
                userId,
                "DRIVER_ASSIGNED",
                "Dispatch",
                dispatch.getId(),
                "Assigned driver " + request.getDriverId());

        return dispatchMapper.toResponseDTO(dispatch);
    }
    @Override
    public DispatchResponseDTO assignVehicle(String dispatchId,
                                             AssignVehicleRequestDTO request,
                                             String userId) {

        log.info("Assigning vehicle {} to dispatch {}", request.getVehicleId(), dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);

        if (dispatch.getStatus() != DispatchStatus.PENDING)
               {
            throw new DispatchStateException(
                    "Vehicle can only be assigned while dispatch is PENDING or DRIVER_ASSIGNED");
        }




        DispatchStatus previous = dispatch.getStatus();



        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(
                dispatch,
                previous,
                dispatch.getStatus(),
                userId,
                "Vehicle assigned");

        notificationService.notifyDispatchAssigned(dispatch);

        auditService.logAction(
                userId,
                "VEHICLE_ASSIGNED",
                "Dispatch",
                dispatch.getId(),
                "Assigned vehicle " + request.getVehicleId());

        return dispatchMapper.toResponseDTO(dispatch);
    }




    @Override
    @Transactional
    public DispatchResponseDTO reassignDispatch(String dispatchId, String userId) {

        log.info("Reassigning dispatch {}", dispatchId);

        Dispatch dispatch = findDispatch(dispatchId);

        if (!dispatch.getStatus().canBeReassigned()) {
            throw new DispatchStateException(
                    "Dispatch cannot be reassigned from status: "
                            + dispatch.getStatus()
            );
        }

        if (dispatch.getRetryCount() >= MAX_RETRIES) {
            throw new DispatchStateException(
                    "Maximum retry limit reached."
            );
        }

        DispatchStatus previousStatus = dispatch.getStatus();

        /*
         * Release currently allocated resources.
         */
        assignmentOrchestrator.releaseResources(
                dispatch,
                userId,
                "Manual reassignment"
        );

        /*
         * Reset dispatch.
         */
        dispatch.setStatus(DispatchStatus.PENDING);

        dispatch.setDriver(null);
        dispatch.setVehicle(null);

        dispatch.setAssignedAt(null);
        dispatch.setAcceptedAt(null);
        dispatch.setRejectedAt(null);
        dispatch.setCompletedAt(null);

        dispatch.setFailureReason(null);

        dispatch = dispatchRepository.save(dispatch);

        /*
         * Return order to dispatch queue.
         */
        Order order = dispatch.getOrder();

        order.setStatus(OrderStatus.READY_FOR_DISPATCH);

        orderRepository.save(order);

        logDispatchHistory(
                dispatch,
                previousStatus,
                DispatchStatus.PENDING,
                userId,
                "Dispatch queued for reassignment"
        );

        auditService.logAction(
                userId,
                "DISPATCH_REASSIGNED",
                "Dispatch",
                dispatch.getId(),
                "Dispatch queued for reassignment"
        );

        /*
         * Queue for automatic assignment.
         */
        queueService.addToQueue(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }


    @Override
    @Transactional
    public DispatchResponseDTO cancelDispatch(
            String dispatchId,
            String reason,
            String userId) {

        log.info("Cancelling dispatch {}. Reason: {}", dispatchId, reason);

        Driver driver = driverRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found"));

        String driverId = driver.getId();

        Dispatch dispatch = findDispatch(dispatchId);

        validateTransition(
                dispatch.getStatus(),
                DispatchStatus.CANCELLED
        );

        DispatchStatus previousStatus = dispatch.getStatus();

        /*
         * Release any assigned resources.
         */
        assignmentOrchestrator.releaseResources(
                dispatch,
                driverId,
                reason
        );

        /*
         * Cancel dispatch.
         */
        dispatch.setStatus(DispatchStatus.CANCELLED);
        dispatch.setCancelledAt(LocalDateTime.now());
        dispatch.setFailureReason(reason);

        dispatch.setDriver(null);
        dispatch.setVehicle(null);

        dispatch.setAssignedAt(null);
        dispatch.setAcceptedAt(null);

        dispatch = dispatchRepository.save(dispatch);

        /*
         * Reset order.
         */
        Order order = dispatch.getOrder();

        order.setDriver(null);
        order.setStatus(OrderStatus.READY_FOR_DISPATCH);

        orderRepository.save(order);

        logDispatchHistory(
                dispatch,
                previousStatus,
                DispatchStatus.CANCELLED,
                driverId,
                "Dispatch cancelled. Reason: " + reason
        );

        auditService.logAction(
                driverId,
                "DISPATCH_CANCELLED",
                "Dispatch",
                dispatch.getId(),
                reason
        );

        notificationService.notifyDispatchCancelled(
                dispatch,
                reason
        );

        eventPublisher.publishDispatchCancelled(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    @Transactional
    public DispatchResponseDTO acceptDispatch(String dispatchId, String userId) {

        log.info("ID RECEIVED = {}", userId);
        log.info("Driver {} accepting dispatch {}", userId, dispatchId);

        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found"));

        log.info("FOUND DRIVER = {}", driver);
        log.info("DRIVER ID = {}", driver.getId());

        String driverId = driver.getId();

        Dispatch dispatch = findDispatch(dispatchId);

        validateTransition(
                dispatch.getStatus(),
                DispatchStatus.DRIVER_ACCEPTED
        );

        if (dispatch.getDriver() == null) {
            throw new DispatchStateException(
                    "Dispatch has no assigned driver."
            );
        }

        if (dispatch.getVehicle() == null) {
            throw new DispatchStateException(
                    "Dispatch has no assigned vehicle."
            );
        }

        if (!dispatch.getDriver().getId().equals(driverId)) {
            throw new DispatchStateException(
                    "You are not assigned to this dispatch."
            );
        }

        DispatchStatus previousStatus = dispatch.getStatus();

        dispatch.setStatus(DispatchStatus.DRIVER_ACCEPTED);
        dispatch.setAcceptedAt(LocalDateTime.now());

        Order order = dispatch.getOrder();
        order.setDriver(dispatch.getDriver());
        order.setStatus(OrderStatus.DISPATCH);

        orderRepository.save(order);

        dispatch = dispatchRepository.save(dispatch);

        // Start tracking - wrapped in try-catch to prevent dispatch acceptance failure
        try {
            StartTrackingRequestDTO trackingRequest = new StartTrackingRequestDTO();
            trackingRequest.setOrderId(order.getId());
            trackingRequest.setDriverId(driverId);

            log.info("Starting tracking with orderId: {} and driverId: {}", order.getId(), driverId);

            trackingService.startTracking(trackingRequest, driverId);
            log.info("Tracking started successfully for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to start tracking for order {}: {}", order.getId(), e.getMessage(), e);
            // Don't throw - dispatch acceptance should still succeed
            // The tracking can be started later
        }

        notificationService.notifyDispatchAccepted(dispatch);
        eventPublisher.publishDispatchAccepted(dispatch);

        logDispatchHistory(
                dispatch,
                previousStatus,
                DispatchStatus.DRIVER_ACCEPTED,
                driverId,
                "Dispatch accepted by driver"
        );

        auditService.logAction(
                driverId,
                "DISPATCH_ACCEPTED",
                "Dispatch",
                dispatch.getId(),
                "Driver accepted dispatch"
        );

        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    @Transactional
    public DispatchResponseDTO rejectDispatch(
            String dispatchId,
            String reason,
            String UserId) {

        log.info("Driver {} rejected dispatch {}", UserId, dispatchId);

        Driver driver = driverRepository.findByUserId(UserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found"));

        String driverId = driver.getId();

        Dispatch dispatch = findDispatch(dispatchId);

        validateTransition(
                dispatch.getStatus(),
                DispatchStatus.FAILED
        );

        if (dispatch.getDriver() == null) {
            throw new DispatchStateException(
                    "Dispatch has no assigned driver."
            );
        }

        if (!dispatch.getDriver().getId().equals(driverId)) {
            throw new DispatchStateException(
                    "You are not assigned to this dispatch."
            );
        }

        DispatchStatus previousStatus = dispatch.getStatus();

        dispatch.setStatus(DispatchStatus.FAILED);
        dispatch.setRejectedAt(LocalDateTime.now());
        dispatch.setFailureReason(reason);
        dispatch.setRetryCount(dispatch.getRetryCount() + 1);

        dispatch = dispatchRepository.save(dispatch);

        logDispatchHistory(
                dispatch,
                previousStatus,
                DispatchStatus.FAILED,
                driverId,
                "Dispatch rejected. Reason: " + reason
        );

        assignmentOrchestrator.releaseResources(
                dispatch,
                driverId,
                reason
        );

        Order order = dispatch.getOrder();
        order.setStatus(OrderStatus.READY_FOR_DISPATCH);
        orderRepository.save(order);

        auditService.logAction(
                driverId,
                "DISPATCH_REJECTED",
                "Dispatch",
                dispatch.getId(),
                reason
        );

        notificationService.notifyDispatchRejected(dispatch, reason);
        eventPublisher.publishDispatchRejected(dispatch);

        if (dispatch.getRetryCount() < MAX_RETRIES) {

            dispatch.setStatus(DispatchStatus.PENDING);

            dispatch.setDriver(null);
            dispatch.setVehicle(null);

            dispatch.setAssignedAt(null);
            dispatch.setAcceptedAt(null);

            dispatch = dispatchRepository.save(dispatch);

            queueService.addToQueue(dispatch);

            logDispatchHistory(
                    dispatch,
                    DispatchStatus.FAILED,
                    DispatchStatus.PENDING,
                    driverId,
                    "Queued for reassignment"
            );
        }

        return dispatchMapper.toResponseDTO(dispatch);
    }
    @Override
    @Transactional
    public DispatchResponseDTO completeDispatch(String dispatchId, String userId) {

        log.info("Completing dispatch {}", dispatchId);

        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found"));

        String driverId = driver.getId();

        Dispatch dispatch = findDispatch(dispatchId);

        if (dispatch.getStatus() == DispatchStatus.DELIVERED) {
            throw new DispatchStateException(
                    "Dispatch has already been completed."
            );
        }

        validateTransition(
                dispatch.getStatus(),
                DispatchStatus.DELIVERED
        );

        if (dispatch.getDriver() == null) {
            throw new DispatchStateException(
                    "Dispatch has no assigned driver."
            );
        }

        if (!dispatch.getDriver().getId().equals(driverId)) {
            throw new DispatchStateException(
                    "Only the assigned driver can complete this dispatch."
            );
        }

        DispatchStatus previousStatus = dispatch.getStatus();

        dispatch.setStatus(DispatchStatus.DELIVERED);
        dispatch.setCompletedAt(LocalDateTime.now());

        dispatch = dispatchRepository.save(dispatch);

        Order order = dispatch.getOrder();
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        assignmentOrchestrator.releaseResources(
                dispatch,
                driverId,
                "Delivery completed"
        );

        logDispatchHistory(
                dispatch,
                previousStatus,
                DispatchStatus.DELIVERED,
                driverId,
                "Dispatch completed"
        );

        auditService.logAction(
                driverId,
                "DISPATCH_COMPLETED",
                "Dispatch",
                dispatch.getId(),
                "Dispatch completed successfully"
        );

        notificationService.notifyDispatchCompleted(dispatch);
        eventPublisher.publishDispatchCompleted(dispatch);

        return dispatchMapper.toResponseDTO(dispatch);
    }





    @Override
    public DispatchResponseDTO getDispatchById(String dispatchId) {
        Dispatch dispatch = findDispatch(dispatchId);
        return dispatchMapper.toResponseDTO(dispatch);
    }

    @Override
    public DispatchResponseDTO getDispatchByOrder(String orderId) {

        log.info("Looking for dispatch using orderId={}", orderId);

        Optional<Dispatch> optional =
                dispatchRepository.findByOrderId(orderId);

        log.info("Dispatch present = {}", optional.isPresent());

        if (optional.isEmpty()) {

            log.info("Total dispatches = {}", dispatchRepository.count());

            dispatchRepository.findAll()
                    .forEach(d ->
                            log.info(
                                    "Dispatch {} order={} status={}",
                                    d.getId(),
                                    d.getOrder().getId(),
                                    d.getStatus()
                            ));

            throw new DispatchNotFoundException(
                    "No dispatch found for order: " + orderId
            );
        }

        return dispatchMapper.toResponseDTO(optional.get());
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

    private void validateTransition(
            DispatchStatus currentStatus,
            DispatchStatus targetStatus) {

        if (stateValidator.isValidTransition(currentStatus, targetStatus)) {
            return;
        }

        throw new DispatchStateException(
                String.format(
                        "Cannot transition dispatch from %s to %s.",
                        currentStatus,
                        targetStatus
                )
        );
    }


    private Dispatch findDispatch(String dispatchId) {

        return dispatchRepository.findById(dispatchId)
                .orElseThrow(() ->
                        new DispatchNotFoundException(
                                "Dispatch not found with id: " + dispatchId
                        )
                );
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

    private Order getOrder(Dispatch dispatch) {
        return dispatch.getOrder();
    }

    private Driver getDriver(Dispatch dispatch) {

        if (dispatch.getDriver() == null) {
            throw new DispatchStateException("No driver assigned.");
        }

        return dispatch.getDriver();
    }

    private Vehicle getVehicle(Dispatch dispatch) {

        if (dispatch.getVehicle() == null) {
            throw new DispatchStateException("No vehicle assigned.");
        }

        return dispatch.getVehicle();
    }

    @Override
    @Transactional(readOnly = true)
    public DispatchSummaryDTO getCurrentDispatchForDriver(String driverId) {

        return dispatchRepository
                .findCurrentDispatch(driverId)
                .map(dispatchMapper::toSummaryDTO)
                .orElse(null);
    }


    @Override
    @Transactional(readOnly = true)
    public long countActiveDispatches(String driverId) {

        return dispatchRepository
                .findActiveDispatchesByDriverId(driverId)
                .size();
    }
    @Override
    @Transactional(readOnly = true)
    public long countCompletedDispatches(String driverId) {

        return dispatchRepository
                .findCompletedDispatchesByDriverId(driverId)
                .size();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<DispatchSummaryDTO> getMyDispatches(
            String userId,
            int page,
            int size) {

        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found"));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Dispatch> dispatches =
                dispatchRepository.findByDriverId(driver.getId(), pageable);

        return toPaginatedResponse(dispatches);
    }
}