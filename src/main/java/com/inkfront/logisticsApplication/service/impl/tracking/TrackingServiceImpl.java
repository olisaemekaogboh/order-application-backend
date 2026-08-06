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
    public TrackingSessionResponseDTO startTracking(StartTrackingRequestDTO request, String userId) {
        log.info("Starting tracking for order: {} by user: {}", request.getOrderId(), userId);

        // Validate input
        if (!StringUtils.hasText(request.getOrderId()) || !StringUtils.hasText(request.getDriverId())) {
            throw new IllegalArgumentException("Order ID and Driver ID are required");
        }

        // Validate order
        Order order = trackingValidator.validateOrder(request.getOrderId());

        // Check if user is admin - handle enum properly
        boolean isAdmin = false;
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                UserRole role = user.getRole();
                log.info("User role: {}", role);
                // Check if role is ADMIN - UserRole is an enum
                isAdmin = UserRole.ADMIN.equals(role);
            }
        } catch (Exception e) {
            log.warn("Could not check admin status for user {}: {}", userId, e.getMessage());
        }

        log.info("Is admin: {}, User ID: {}, Order Owner: {}", isAdmin, userId, order.getUser().getId());

        // Allow admin or the order owner to start tracking
        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            log.warn("User {} does not own order {} and is not admin", userId, request.getOrderId());
            throw new AccessDeniedException("You do not own this order");
        }

        // Check if tracking already exists
        if (trackingSessionRepository.findByOrderId(order.getId()).isPresent()) {
            throw new InvalidTrackingStateException("Tracking already started for this order");
        }

        // Validate driver
        Driver driver = trackingValidator.validateDriver(request.getDriverId());

        if (order.getDriver() == null) {
            throw new InvalidTrackingStateException(
                    "No driver assigned to this order.");
        }

        if (!order.getDriver().getId().equals(driver.getId())) {
            throw new InvalidTrackingStateException(
                    "Driver is not assigned to this order.");
        }


        // Create tracking session
        TrackingSession session = new TrackingSession();
        session.setOrder(order);
        session.setDriver(driver);
        session.setStatus(TrackingStatus.CREATED);
        session.setStartTime(LocalDateTime.now());
        session.setLastUpdateTime(LocalDateTime.now());
        session.setCurrentLatitude(driver.getCurrentLatitude());
        session.setCurrentLongitude(driver.getCurrentLongitude());

        session = trackingSessionRepository.save(session);

        // Log status change event
        eventService.logStatusChange(session.getId(), null, TrackingStatus.CREATED, "Tracking started", userId);

        // Audit
        auditService.logAction(userId, "TRACKING_STARTED", "TrackingSession", session.getId(),
                "Started tracking for order " + order.getOrderNumber());

        // Notify
        notificationService.sendSystemNotification(order.getUser().getId(), "Tracking Started",
                "Your order " + order.getOrderNumber() + " is now being tracked.");
        notificationService.sendSystemNotification(driver.getId(), "New Tracking",
                "You have been assigned to track order " + order.getOrderNumber());

        // Publish event
        eventPublisher.publishTrackingStarted(session);

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

    @Override
    public TrackingSessionResponseDTO updateStatus(StatusUpdateRequestDTO request, String userId) {
        log.info("Updating status for tracking: {} to {}", request.getTrackingId(), request.getStatus());

        if (!StringUtils.hasText(request.getTrackingId()) || request.getStatus() == null) {
            throw new IllegalArgumentException("Tracking ID and status are required");
        }

        TrackingSession session = findSession(request.getTrackingId());

        // Check authorization
        boolean isDriver = session.getDriver().getId().equals(userId);
        boolean isAdmin = userRepository.findById(userId)
                .map(u -> u.getRole().name().contains("ADMIN"))
                .orElse(false);
        if (!isDriver && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to update this tracking");
        }

        // Validate transition
        stateValidator.validateTransition(session.getStatus(), request.getStatus());

        TrackingStatus oldStatus = session.getStatus();
        session.setStatus(request.getStatus());
        session.setLastUpdateTime(LocalDateTime.now());

        // Calculate ETA if status is IN_TRANSIT
        if (request.getStatus() == TrackingStatus.IN_TRANSIT && request.getLatitude() != null && request.getLongitude() != null) {
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
        if (stateValidator.isTerminal(request.getStatus())) {
            session.setEndTime(LocalDateTime.now());
            if (request.getStatus() == TrackingStatus.DELIVERED) {
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
                request.getStatus(),
                request.getDescription(),
                userId
        );

        // Audit
        auditService.logAction(userId, "TRACKING_STATUS_CHANGED", "TrackingSession", session.getId(),
                "Status changed from " + oldStatus + " to " + request.getStatus());

        // Notifications
        if (request.getStatus() == TrackingStatus.PICKED_UP) {
            notificationService.sendSystemNotification(session.getOrder().getUser().getId(), "Order Picked Up",
                    "Your order " + session.getOrder().getOrderNumber() + " has been picked up.");
        } else if (request.getStatus() == TrackingStatus.DELIVERED) {
            notificationService.sendSystemNotification(session.getOrder().getUser().getId(), "Order Delivered",
                    "Your order " + session.getOrder().getOrderNumber() + " has been delivered.");
            notificationService.sendSystemNotification(session.getDriver().getId(), "Delivery Completed",
                    "You have successfully delivered order " + session.getOrder().getOrderNumber());
        } else if (request.getStatus() == TrackingStatus.CANCELLED) {
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

        log.info("Status updated for tracking: {} to {}", request.getTrackingId(), request.getStatus());
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

    @Override
    public TrackingSessionResponseDTO getTrackingByOrder(String orderId) {
        if (!StringUtils.hasText(orderId)) {
            throw new IllegalArgumentException("Order ID is required");
        }
        TrackingSession session = trackingSessionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new TrackingNotFoundException("No tracking session found for order: " + orderId));
        return trackingMapper.toResponseDTO(session);
    }

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