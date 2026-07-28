package com.inkfront.logisticsApplication.service.interfaces.tracking;

import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingEvent;
import com.inkfront.logisticsApplication.domain.enums.TrackingStatus;
import com.inkfront.logisticsApplication.dto.response.tracking.TrackingEventDTO;

import java.util.List;

public interface TrackingEventService {

    TrackingEvent logStatusChange(String trackingSessionId, TrackingStatus oldStatus, TrackingStatus newStatus, String description, String userId);

    TrackingEvent logCheckpoint(String trackingSessionId, String checkpointName, Double latitude, Double longitude, String userId);

    List<TrackingEventDTO> getTimeline(String trackingSessionId);
}