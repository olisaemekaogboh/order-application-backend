package com.inkfront.logisticsApplication.service.impl.tracking;

import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingEvent;
import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingLocation;
import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingSession;
import com.inkfront.logisticsApplication.domain.enums.TrackingStatus;
import com.inkfront.logisticsApplication.domain.enums.UserRole;
import com.inkfront.logisticsApplication.dto.request.tracking.*;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.tracking.*;
import com.inkfront.logisticsApplication.events.publisher.TrackingEventPublisher;
import com.inkfront.logisticsApplication.exception.ResourceNotFoundException;
import com.inkfront.logisticsApplication.exception.tracking.InvalidLocationException;
import com.inkfront.logisticsApplication.exception.tracking.InvalidTrackingStateException;
import com.inkfront.logisticsApplication.exception.tracking.TrackingNotFoundException;
import com.inkfront.logisticsApplication.mapper.tracking.TrackingMapper;
import com.inkfront.logisticsApplication.repository.OrderRepository;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.repository.tracking.TrackingSessionRepository;
import com.inkfront.logisticsApplication.repository.tracking.TrackingLocationRepository;
import com.inkfront.logisticsApplication.repository.tracking.TrackingEventRepository;
import com.inkfront.logisticsApplication.service.interfaces.DistanceService;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import com.inkfront.logisticsApplication.service.interfaces.tracking.TrackingService;
import com.inkfront.logisticsApplication.service.interfaces.tracking.TrackingLocationService;
import com.inkfront.logisticsApplication.service.interfaces.tracking.TrackingEventService;
import com.inkfront.logisticsApplication.validator.tracking.LocationValidator;
import com.inkfront.logisticsApplication.validator.tracking.TrackingStateValidator;
import com.inkfront.logisticsApplication.validator.tracking.TrackingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrackingServiceImpl implements TrackingService {

    private final TrackingSessionRepository trackingSessionRepository;
    private final TrackingLocationRepository trackingLocationRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final TrackingMapper trackingMapper;
    private final TrackingValidator trackingValidator;
    private final LocationValidator locationValidator;
    private final TrackingStateValidator stateValidator;
    private final TrackingLocationService locationService;
    private final TrackingEventService eventService;
    private final TrackingEventPublisher eventPublisher;
    private final TrackingWebSocketService webSocketService;
    private final DistanceService distanceService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    // ==================== Core Operations ====================

    @Override
    @Transactional
    public TrackingSessionResponseDTO startTracking(StartTrackingRequestDTO request, String userId) {
        log.info("Starting tracking for order: {} by user: {}", request.getOrderId(), userId);

        // Validate input
        if (!StringUtils.hasText(request.getOrderId()) || !StringUtils.hasText(request.getDriverId())) {
            throw new IllegalArgumentException("Order ID and Driver ID are required");
        }

        // Validate order
        Order order = trackingValidator.validateOrder(request.getOrderId());

        // Check if user is admin
        boolean isAdmin = false;
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                UserRole role = user.getRole();
                log.info("User role: {}", role);
                isAdmin = UserRole.ADMIN.equals(role) || UserRole.SUPER_ADMIN.equals(role);
            }
        } catch (Exception e) {
            log.warn("Could not check admin status for user {}: {}", userId, e.getMessage());
        }

        // Validate driver
        Driver driver = trackingValidator.validateDriver(request.getDriverId());
        log.info("Driver found: {}", driver.getId());

        // Check if the driver is the assigned driver for this order
        boolean isAssignedDriver = order.getDriver() != null &&
                order.getDriver().getId().equals(driver.getId());

        log.info("Is admin: {}, Is assigned driver: {}, User ID: {}, Order Owner: {}",
                isAdmin, isAssignedDriver, userId, order.getUser().getId());

        // Allow admin, the assigned driver, or the order owner to start tracking
        if (!isAdmin && !isAssignedDriver && !order.getUser().getId().equals(userId)) {
            log.warn("User {} is not authorized to start tracking for order {}", userId, request.getOrderId());
            throw new AccessDeniedException("You are not authorized to start tracking for this order");
        }

        // Check if tracking already exists - return existing if found
        Optional<TrackingSession> existingSession = trackingSessionRepository.findByOrderId(order.getId());
        if (existingSession.isPresent()) {
            log.info("Tracking already exists for order: {}, returning existing session with ID: {}",
                    request.getOrderId(), existingSession.get().getId());
            return trackingMapper.toResponseDTO(existingSession.get());
        }

        if (order.getDriver() == null) {
            throw new InvalidTrackingStateException("No driver assigned to this order.");
        }

        if (!order.getDriver().getId().equals(driver.getId())) {
            throw new InvalidTrackingStateException("Driver is not assigned to this order.");
        }

        // Create tracking session
        TrackingSession session = new TrackingSession();
        session.setOrder(order);
        session.setDriver(driver);
        // ✅ Start with DRIVER_ACCEPTED instead of CREATED
        // This aligns with the dispatch acceptance workflow
        session.setStatus(TrackingStatus.DRIVER_ACCEPTED);
        session.setStartTime(LocalDateTime.now());
        session.setLastUpdateTime(LocalDateTime.now());
        session.setCurrentLatitude(driver.getCurrentLatitude());
        session.setCurrentLongitude(driver.getCurrentLongitude());

        // IMPORTANT: Set the version to 0L for new entity
        session.setVersion(0L);

        session = trackingSessionRepository.save(session);
        log.info("Created tracking session with ID: {}", session.getId());

        // Log status change event - wrapped to prevent failure
        try {
            eventService.logStatusChange(session.getId(), null, TrackingStatus.DRIVER_ACCEPTED,
                    "Tracking started - Driver accepted dispatch", userId);
            log.info("Status change event logged successfully");
        } catch (Exception e) {
            log.warn("Could not log status change event: {}", e.getMessage());
        }

        // Audit - wrapped to prevent failure
        try {
            auditService.logAction(userId, "TRACKING_STARTED", "TrackingSession", session.getId(),
                    "Started tracking for order " + order.getOrderNumber());
            log.info("Audit logged successfully");
        } catch (Exception e) {
            log.warn("Could not log audit: {}", e.getMessage());
        }

        // Notify - wrapped to prevent failure
        try {
            notificationService.sendSystemNotification(order.getUser().getId(), "Tracking Started",
                    "Your order " + order.getOrderNumber() + " is now being tracked.");
            notificationService.sendSystemNotification(driver.getId(), "New Tracking",
                    "You have been assigned to track order " + order.getOrderNumber());
            log.info("Notifications sent successfully");
        } catch (Exception e) {
            log.warn("Could not send notifications: {}", e.getMessage());
        }

        // Publish event - wrapped to prevent failure
        try {
            eventPublisher.publishTrackingStarted(session);
            log.info("Event published successfully");
        } catch (Exception e) {
            log.warn("Could not publish event: {}", e.getMessage());
        }

        log.info("Tracking started with ID: {}", session.getId());
        return trackingMapper.toResponseDTO(session);
    }

    @Override
    public TrackingSessionResponseDTO updateLocation(LocationUpdateRequestDTO request, String userId) {
        log.info("Updating location for tracking: {}", request.getTrackingId());

        // Validate request
        if (!StringUtils.hasText(request.getTrackingId())) {
            throw new IllegalArgumentException("Tracking ID is required");
        }
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new InvalidLocationException("Latitude and longitude are required");
        }

        // Validate coordinates
        locationValidator.validateCoordinates(request.getLatitude(), request.getLongitude());
        locationValidator.validateAccuracy(request.getAccuracy());
        locationValidator.validateSpeed(request.getSpeed());

        TrackingSession session = findSession(request.getTrackingId());

        // Check if user is the assigned driver or admin
        if (!session.getDriver().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to update this tracking");
        }

        // Update session location
        session.setCurrentLatitude(request.getLatitude());
        session.setCurrentLongitude(request.getLongitude());
        session.setLastUpdateTime(LocalDateTime.now());

        // Save location history
        locationService.saveLocation(request, session.getId());

        // Update distance traveled if we have previous location
        if (session.getCurrentLatitude() != null && session.getCurrentLongitude() != null) {
            double prevLat = session.getCurrentLatitude();
            double prevLon = session.getCurrentLongitude();
            double distance = distanceService.calculateDistance(prevLat, prevLon, request.getLatitude(), request.getLongitude());
            if (distance > 0.001) { // only add meaningful distances
                session.setDistanceTraveledKm(session.getDistanceTraveledKm() + distance);
            }
        }

        session = trackingSessionRepository.save(session);

        // Send WebSocket update
        LiveTrackingDTO liveDTO = buildLiveTrackingDTO(session);
        webSocketService.sendLiveUpdate(session.getId(), liveDTO);
        webSocketService.sendToUser(session.getOrder().getUser().getId(), liveDTO);
        webSocketService.sendToUser(session.getDriver().getId(), liveDTO);

        // Publish event
        eventPublisher.publishTrackingLocationUpdated(session);

        log.info("Location updated for tracking: {}", request.getTrackingId());
        return trackingMapper.toResponseDTO(session);
    }

    /**
     * Updates the status of a tracking session with automatic handling of CREATED status.
     *
     * <p>This method includes auto-migration logic for existing sessions that were created
     * with status CREATED. If a session is in CREATED status and attempting to transition
     * to DRIVER_EN_ROUTE_TO_PICKUP, it will first transition to DRIVER_ACCEPTED
     * automatically, then proceed to the target status.</p>
     *
     * @param request The status update request containing tracking ID and target status
     * @param userId The ID of the user making the request
     * @return The updated tracking session response
     * @throws InvalidTrackingStateException If the status transition is invalid
     * @throws AccessDeniedException If the user is not authorized
     */
    // com/inkfront/logisticsApplication/service/impl/tracking/TrackingServiceImpl.java

    @Override
    public TrackingSessionResponseDTO updateStatus(StatusUpdateRequestDTO request, String userId) {
        log.info("Updating status for tracking: {} to {}", request.getTrackingId(), request.getStatus());

        if (!StringUtils.hasText(request.getTrackingId()) || request.getStatus() == null) {
            throw new IllegalArgumentException("Tracking ID and status are required");
        }

        TrackingSession session = findSession(request.getTrackingId());
        TrackingStatus currentStatus = session.getStatus();
        TrackingStatus targetStatus = request.getStatus();

        // ✅ ALLOW IDEMPOTENT STATUS UPDATES (same status)
        if (currentStatus == targetStatus) {
            log.info("Tracking {} is already in status {}, returning current session (idempotent)",
                    request.getTrackingId(), targetStatus);
            return trackingMapper.toResponseDTO(session);
        }

        // Check authorization
        boolean isDriver = session.getDriver().getId().equals(userId);
        boolean isAdmin = userRepository.findById(userId)
                .map(u -> u.getRole().name().contains("ADMIN"))
                .orElse(false);
        if (!isDriver && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to update this tracking");
        }

        // ✅ FIX: Handle CREATED status migration
        if (currentStatus == TrackingStatus.CREATED &&
                targetStatus == TrackingStatus.DRIVER_EN_ROUTE_TO_PICKUP) {
            log.info("Auto-migrating CREATED session {} to DRIVER_ACCEPTED first", session.getId());

            // Update to DRIVER_ACCEPTED
            session.setStatus(TrackingStatus.DRIVER_ACCEPTED);
            session.setLastUpdateTime(LocalDateTime.now());
            session = trackingSessionRepository.save(session);

            // Log the intermediate transition
            try {
                eventService.logStatusChange(
                        session.getId(),
                        TrackingStatus.CREATED,
                        TrackingStatus.DRIVER_ACCEPTED,
                        "Auto-migration from CREATED to DRIVER_ACCEPTED",
                        userId
                );
            } catch (Exception e) {
                log.warn("Could not log auto-migration event: {}", e.getMessage());
            }

            // Now validate the transition from DRIVER_ACCEPTED to the target
            stateValidator.validateTransition(TrackingStatus.DRIVER_ACCEPTED, targetStatus);
            currentStatus = TrackingStatus.DRIVER_ACCEPTED;
        } else {
            // Original validation
            stateValidator.validateTransition(currentStatus, targetStatus);
        }

        TrackingStatus oldStatus = currentStatus;
        session.setStatus(targetStatus);
        session.setLastUpdateTime(LocalDateTime.now());

        // Calculate ETA if status is IN_TRANSIT
        if (targetStatus == TrackingStatus.IN_TRANSIT && request.getLatitude() != null && request.getLongitude() != null) {
            double distance = distanceService.calculateDistance(
                    request.getLatitude(), request.getLongitude(),
                    session.getOrder().getDeliveryLatitude(), session.getOrder().getDeliveryLongitude()
            );
            long estimatedMinutes = distanceService.estimateTravelTime(distance, session.getDriver().getVehicleType().name());
            session.setEstimatedArrival(LocalDateTime.now().plusMinutes(estimatedMinutes));
            // Update location as well
            session.setCurrentLatitude(request.getLatitude());
            session.setCurrentLongitude(request.getLongitude());
        }

        // Special handling for terminal states
        if (stateValidator.isTerminal(targetStatus)) {
            session.setEndTime(LocalDateTime.now());
            if (targetStatus == TrackingStatus.DELIVERED) {
                session.setActualArrival(LocalDateTime.now());
                session.getOrder().setDeliveryDate(LocalDateTime.now());
                orderRepository.save(session.getOrder());
            }
        }

        session = trackingSessionRepository.save(session);

        // Log event
        eventService.logStatusChange(
                session.getId(),
                oldStatus,
                targetStatus,
                request.getDescription(),
                userId
        );

        // Audit
        auditService.logAction(userId, "TRACKING_STATUS_CHANGED", "TrackingSession", session.getId(),
                "Status changed from " + oldStatus + " to " + targetStatus);

        // Notifications
        if (targetStatus == TrackingStatus.PICKED_UP) {
            notificationService.sendSystemNotification(session.getOrder().getUser().getId(), "Order Picked Up",
                    "Your order " + session.getOrder().getOrderNumber() + " has been picked up.");
        } else if (targetStatus == TrackingStatus.DELIVERED) {
            notificationService.sendSystemNotification(session.getOrder().getUser().getId(), "Order Delivered",
                    "Your order " + session.getOrder().getOrderNumber() + " has been delivered.");
            notificationService.sendSystemNotification(session.getDriver().getId(), "Delivery Completed",
                    "You have successfully delivered order " + session.getOrder().getOrderNumber());
        } else if (targetStatus == TrackingStatus.CANCELLED) {
            notificationService.sendSystemNotification(session.getOrder().getUser().getId(), "Order Cancelled",
                    "Your order " + session.getOrder().getOrderNumber() + " has been cancelled.");
        }

        // WebSocket
        LiveTrackingDTO liveDTO = buildLiveTrackingDTO(session);
        webSocketService.sendLiveUpdate(session.getId(), liveDTO);
        webSocketService.sendToUser(session.getOrder().getUser().getId(), liveDTO);
        webSocketService.sendToUser(session.getDriver().getId(), liveDTO);

        // Publish event
        eventPublisher.publishTrackingStatusChanged(session);

        log.info("Status updated for tracking: {} to {}", request.getTrackingId(), targetStatus);
        return trackingMapper.toResponseDTO(session);
    }

    @Override
    public TrackingSessionResponseDTO completeTracking(CompleteTrackingRequestDTO request, String userId) {
        log.info("Completing tracking: {}", request.getTrackingId());

        if (!StringUtils.hasText(request.getTrackingId())) {
            throw new IllegalArgumentException("Tracking ID is required");
        }

        TrackingSession session = findSession(request.getTrackingId());

        if (!session.getDriver().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to complete this tracking");
        }

        stateValidator.validateTransition(session.getStatus(), TrackingStatus.DELIVERED);

        // ✅ FIX: If session is CREATED, auto-migrate to DRIVER_ACCEPTED first
        if (session.getStatus() == TrackingStatus.CREATED) {
            log.info("Auto-migrating CREATED session {} to DRIVER_ACCEPTED before completing", session.getId());
            session.setStatus(TrackingStatus.DRIVER_ACCEPTED);
            session = trackingSessionRepository.save(session);
        }

        session.setStatus(TrackingStatus.DELIVERED);
        session.setEndTime(LocalDateTime.now());
        session.setActualArrival(LocalDateTime.now());
        session.setCompletedAt(LocalDateTime.now());
        session = trackingSessionRepository.save(session);

        // Update order
        Order order = session.getOrder();
        order.setDeliveryDate(LocalDateTime.now());
        orderRepository.save(order);

        // Log event
        eventService.logStatusChange(session.getId(), session.getStatus(), TrackingStatus.DELIVERED,
                "Tracking completed: " + (request.getCompletionNotes() != null ? request.getCompletionNotes() : ""), userId);

        // Audit
        auditService.logAction(userId, "TRACKING_COMPLETED", "TrackingSession", session.getId(), "Tracking completed");

        // Notifications
        notificationService.sendSystemNotification(session.getOrder().getUser().getId(), "Order Delivered",
                "Your order " + order.getOrderNumber() + " has been delivered.");

        // WebSocket
        LiveTrackingDTO liveDTO = buildLiveTrackingDTO(session);
        webSocketService.sendLiveUpdate(session.getId(), liveDTO);
        webSocketService.sendToUser(session.getOrder().getUser().getId(), liveDTO);

        eventPublisher.publishTrackingCompleted(session);

        return trackingMapper.toResponseDTO(session);
    }

    @Override
    public TrackingSessionResponseDTO cancelTracking(CancelTrackingRequestDTO request, String userId) {
        log.info("Cancelling tracking: {}", request.getTrackingId());

        if (!StringUtils.hasText(request.getTrackingId()) || !StringUtils.hasText(request.getReason())) {
            throw new IllegalArgumentException("Tracking ID and reason are required");
        }

        TrackingSession session = findSession(request.getTrackingId());

        // Allow admin or the user who started it
        boolean isAdmin = userRepository.findById(userId)
                .map(u -> u.getRole().name().contains("ADMIN"))
                .orElse(false);
        if (!isAdmin && !session.getOrder().getUser().getId().equals(userId) && !session.getDriver().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to cancel this tracking");
        }

        if (stateValidator.isTerminal(session.getStatus())) {
            throw new InvalidTrackingStateException("Cannot cancel a completed tracking");
        }

        // ✅ FIX: If session is CREATED, auto-migrate to DRIVER_ACCEPTED first
        if (session.getStatus() == TrackingStatus.CREATED) {
            log.info("Auto-migrating CREATED session {} to DRIVER_ACCEPTED before cancelling", session.getId());
            session.setStatus(TrackingStatus.DRIVER_ACCEPTED);
            session = trackingSessionRepository.save(session);
        }

        stateValidator.validateTransition(session.getStatus(), TrackingStatus.CANCELLED);

        session.setStatus(TrackingStatus.CANCELLED);
        session.setEndTime(LocalDateTime.now());
        session.setCancellationReason(request.getReason());
        session = trackingSessionRepository.save(session);

        // Log event
        eventService.logStatusChange(session.getId(), session.getStatus(), TrackingStatus.CANCELLED,
                "Tracking cancelled: " + request.getReason(), userId);

        // Audit
        auditService.logAction(userId, "TRACKING_CANCELLED", "TrackingSession", session.getId(),
                "Tracking cancelled: " + request.getReason());

        // Notifications
        notificationService.sendSystemNotification(session.getOrder().getUser().getId(), "Tracking Cancelled",
                "Tracking for order " + session.getOrder().getOrderNumber() + " has been cancelled.");

        // WebSocket
        LiveTrackingDTO liveDTO = buildLiveTrackingDTO(session);
        webSocketService.sendLiveUpdate(session.getId(), liveDTO);
        webSocketService.sendToUser(session.getOrder().getUser().getId(), liveDTO);

        eventPublisher.publishTrackingCancelled(session);

        return trackingMapper.toResponseDTO(session);
    }

    // ==================== Retrieval Methods ====================

    @Override
    public TrackingSessionResponseDTO getTrackingById(String trackingId) {
        if (!StringUtils.hasText(trackingId)) {
            throw new IllegalArgumentException("Tracking ID is required");
        }
        TrackingSession session = findSession(trackingId);
        return trackingMapper.toResponseDTO(session);
    }

    @Override
    public LiveTrackingDTO getLiveTracking(String trackingId) {
        if (!StringUtils.hasText(trackingId)) {
            throw new IllegalArgumentException("Tracking ID is required");
        }
        TrackingSession session = findSession(trackingId);
        return buildLiveTrackingDTO(session);
    }

    @Override
    public TrackingTimelineDTO getTimeline(String trackingId) {
        if (!StringUtils.hasText(trackingId)) {
            throw new IllegalArgumentException("Tracking ID is required");
        }
        TrackingSession session = findSession(trackingId);
        List<TrackingEventDTO> events = eventService.getTimeline(trackingId);

        return TrackingTimelineDTO.builder()
                .trackingId(session.getId())
                .orderNumber(session.getOrder().getOrderNumber())
                .events(events)
                .build();
    }

    // ==================== Paginated Queries ====================

    @Override
    public PaginatedResponseDTO<TrackingSessionResponseDTO> getTrackingByUser(String userId, int page, int size, String sortBy, String sortDirection) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User ID is required");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<TrackingSession> sessions = trackingSessionRepository.findByUserId(userId, pageable);
        return toPaginatedResponse(sessions);
    }

    @Override
    public PaginatedResponseDTO<TrackingSessionResponseDTO> getTrackingByDriver(String driverId, int page, int size) {
        if (!StringUtils.hasText(driverId)) {
            throw new IllegalArgumentException("Driver ID is required");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TrackingSession> sessions = trackingSessionRepository.findByDriverId(driverId, pageable);
        return toPaginatedResponse(sessions);
    }

    @Override
    public PaginatedResponseDTO<TrackingSessionResponseDTO> getAllTracking(int page, int size, String status, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<TrackingSession> sessions;
        if (StringUtils.hasText(status)) {
            sessions = trackingSessionRepository.findByStatus(TrackingStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            sessions = trackingSessionRepository.findAll(pageable);
        }
        return toPaginatedResponse(sessions);
    }

    @Override
    public TrackingSessionResponseDTO getTrackingByOrder(String orderId) {
        if (!StringUtils.hasText(orderId)) {
            throw new IllegalArgumentException("Order ID is required");
        }

        log.info("Looking for tracking session by order ID: {}", orderId);

        Optional<TrackingSession> session = trackingSessionRepository.findByOrderId(orderId);

        if (session.isPresent()) {
            log.info("Found tracking session: {}", session.get().getId());
            return trackingMapper.toResponseDTO(session.get());
        } else {
            log.info("No tracking session found for order: {}", orderId);
            return null;
        }
    }

    // ======================== Private Helpers ========================

    private TrackingSession findSession(String trackingId) {
        return trackingSessionRepository.findById(trackingId)
                .orElseThrow(() -> new TrackingNotFoundException("Tracking session not found: " + trackingId));
    }

    private LiveTrackingDTO buildLiveTrackingDTO(TrackingSession session) {
        return LiveTrackingDTO.builder()
                .trackingId(session.getId())
                .orderNumber(session.getOrder().getOrderNumber())
                .status(session.getStatus())
                .latitude(session.getCurrentLatitude())
                .longitude(session.getCurrentLongitude())
                .lastUpdate(session.getLastUpdateTime())
                .driverName(session.getDriver().getUser().getFullName())
                .driverPhone(session.getDriver().getUser().getPhoneNumber())
                .estimatedArrival(session.getEstimatedArrival())
                .etaText(session.getEstimatedArrival() != null ?
                        ChronoUnit.MINUTES.between(LocalDateTime.now(), session.getEstimatedArrival()) + " min" : null)
                .build();
    }

    private PaginatedResponseDTO<TrackingSessionResponseDTO> toPaginatedResponse(Page<TrackingSession> page) {
        List<TrackingSessionResponseDTO> content = page.getContent().stream()
                .map(trackingMapper::toResponseDTO)
                .collect(Collectors.toList());
        return new PaginatedResponseDTO<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    /**
     * Creates a new tracking session with CREATED status.
     *
     * <p><b>Note:</b> This method is deprecated and kept only for backward compatibility.
     * New tracking sessions should be created using {@link #startTracking(StartTrackingRequestDTO, String)}
     * which initializes with DRIVER_ACCEPTED status.</p>
     *
     * @param order The order to track
     * @param driver The driver assigned to the order
     * @param actorId The ID of the user creating the tracking
     * @return The created tracking session
     */
    @Deprecated
    private TrackingSession createTrackingSession(
            Order order,
            Driver driver,
            String actorId) {

        TrackingSession session = new TrackingSession();

        session.setOrder(order);
        session.setDriver(driver);
        session.setStatus(TrackingStatus.CREATED);
        session.setStartTime(LocalDateTime.now());
        session.setLastUpdateTime(LocalDateTime.now());
        session.setCurrentLatitude(driver.getCurrentLatitude());
        session.setCurrentLongitude(driver.getCurrentLongitude());

        session = trackingSessionRepository.save(session);

        eventService.logStatusChange(
                session.getId(),
                null,
                TrackingStatus.CREATED,
                "Tracking started",
                actorId
        );

        return session;
    }
}