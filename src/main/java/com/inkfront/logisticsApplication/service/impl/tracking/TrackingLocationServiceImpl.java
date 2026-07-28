package com.inkfront.logisticsApplication.service.impl.tracking;

import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingLocation;
import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingSession;
import com.inkfront.logisticsApplication.dto.request.tracking.LocationUpdateRequestDTO;
import com.inkfront.logisticsApplication.exception.tracking.TrackingNotFoundException;
import com.inkfront.logisticsApplication.repository.tracking.TrackingLocationRepository;
import com.inkfront.logisticsApplication.repository.tracking.TrackingSessionRepository;
import com.inkfront.logisticsApplication.service.interfaces.tracking.TrackingLocationService;
import com.inkfront.logisticsApplication.validator.tracking.LocationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrackingLocationServiceImpl implements TrackingLocationService {

    private final TrackingLocationRepository locationRepository;
    private final TrackingSessionRepository sessionRepository;
    private final LocationValidator locationValidator;

    @Override
    public TrackingLocation saveLocation(LocationUpdateRequestDTO request, String trackingSessionId) {
        TrackingSession session = sessionRepository.findById(trackingSessionId)
                .orElseThrow(() -> new TrackingNotFoundException("Session not found: " + trackingSessionId));

        // Mark previous current location as not current
        locationRepository.findCurrentLocationBySessionId(trackingSessionId)
                .ifPresent(prev -> {
                    prev.setCurrent(false);
                    locationRepository.save(prev);
                });

        TrackingLocation location = new TrackingLocation();
        location.setTrackingSession(session);
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAccuracy(request.getAccuracy());
        location.setAltitude(request.getAltitude());
        location.setBearing(request.getBearing());
        location.setSpeed(request.getSpeed());
        location.setProvider(request.getProvider());
        location.setBatteryLevel(request.getBatteryLevel());
        location.setNetworkType(request.getNetworkType());
        location.setTimestamp(LocalDateTime.now());
        location.setCurrent(true);

        return locationRepository.save(location);
    }

    @Override
    public TrackingLocation getCurrentLocation(String trackingSessionId) {
        return locationRepository.findCurrentLocationBySessionId(trackingSessionId)
                .orElse(null);
    }

    @Override
    public List<TrackingLocation> getLocationHistory(String trackingSessionId, int limit) {
        return locationRepository.findByTrackingSessionIdOrderByTimestampDesc(trackingSessionId)
                .stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void cleanupOldLocations(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        // Use a batch delete instead of fetching all records
        locationRepository.deleteByTimestampBefore(cutoff);
        log.info("Cleaned up old tracking locations older than {} days", daysToKeep);
    }
}