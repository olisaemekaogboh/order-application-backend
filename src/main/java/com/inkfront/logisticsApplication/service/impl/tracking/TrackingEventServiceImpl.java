package com.inkfront.logisticsApplication.service.impl.tracking;

import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingEvent;
import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingSession;
import com.inkfront.logisticsApplication.domain.enums.TrackingStatus;
import com.inkfront.logisticsApplication.dto.response.tracking.TrackingEventDTO;
import com.inkfront.logisticsApplication.exception.tracking.TrackingNotFoundException;
import com.inkfront.logisticsApplication.mapper.tracking.TrackingMapper;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.repository.tracking.TrackingEventRepository;
import com.inkfront.logisticsApplication.repository.tracking.TrackingSessionRepository;
import com.inkfront.logisticsApplication.service.interfaces.tracking.TrackingEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrackingEventServiceImpl implements TrackingEventService {

    private final TrackingEventRepository eventRepository;
    private final TrackingSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final TrackingMapper trackingMapper;

    @Override
    public TrackingEvent logStatusChange(String trackingSessionId, TrackingStatus oldStatus, TrackingStatus newStatus,
                                         String description, String userId) {
        TrackingSession session = sessionRepository.findById(trackingSessionId)
                .orElseThrow(() -> new TrackingNotFoundException("Session not found: " + trackingSessionId));

        User performedBy = null;
        if (userId != null) {
            performedBy = userRepository.findById(userId).orElse(null);
        }

        TrackingEvent event = new TrackingEvent();
        event.setTrackingSession(session);
        event.setEventType("STATUS_CHANGE");
        event.setOldStatus(oldStatus);
        event.setNewStatus(newStatus);
        event.setDescription(description);
        event.setPerformedBy(performedBy);
        event.setTimestamp(LocalDateTime.now());

        return eventRepository.save(event);
    }

    @Override
    public TrackingEvent logCheckpoint(String trackingSessionId, String checkpointName, Double latitude,
                                       Double longitude, String userId) {
        TrackingSession session = sessionRepository.findById(trackingSessionId)
                .orElseThrow(() -> new TrackingNotFoundException("Session not found: " + trackingSessionId));

        User performedBy = null;
        if (userId != null) {
            performedBy = userRepository.findById(userId).orElse(null);
        }

        TrackingEvent event = new TrackingEvent();
        event.setTrackingSession(session);
        event.setEventType("CHECKPOINT");
        event.setDescription(checkpointName);
        event.setLatitude(latitude);
        event.setLongitude(longitude);
        event.setPerformedBy(performedBy);
        event.setTimestamp(LocalDateTime.now());

        return eventRepository.save(event);
    }

    @Override
    public List<TrackingEventDTO> getTimeline(String trackingSessionId) {
        List<TrackingEvent> events = eventRepository.findByTrackingSessionIdOrderByTimestampAsc(trackingSessionId);
        return events.stream()
                .map(trackingMapper::toEventDTO)
                .collect(Collectors.toList());
    }
}