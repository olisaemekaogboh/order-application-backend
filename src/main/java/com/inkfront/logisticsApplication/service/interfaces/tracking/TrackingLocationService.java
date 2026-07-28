package com.inkfront.logisticsApplication.service.interfaces.tracking;

import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingLocation;
import com.inkfront.logisticsApplication.dto.request.tracking.LocationUpdateRequestDTO;

import java.util.List;

public interface TrackingLocationService {

    TrackingLocation saveLocation(LocationUpdateRequestDTO request, String trackingSessionId);

    TrackingLocation getCurrentLocation(String trackingSessionId);

    List<TrackingLocation> getLocationHistory(String trackingSessionId, int limit);

    void cleanupOldLocations(int daysToKeep);
}