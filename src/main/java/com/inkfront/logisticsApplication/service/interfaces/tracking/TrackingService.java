package com.inkfront.logisticsApplication.service.interfaces.tracking;

import com.inkfront.logisticsApplication.dto.request.tracking.*;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.tracking.*;

public interface TrackingService {

    TrackingSessionResponseDTO startTracking(StartTrackingRequestDTO request, String userId);

    TrackingSessionResponseDTO updateLocation(LocationUpdateRequestDTO request, String userId);

    TrackingSessionResponseDTO updateStatus(StatusUpdateRequestDTO request, String userId);

    TrackingSessionResponseDTO completeTracking(CompleteTrackingRequestDTO request, String userId);

    TrackingSessionResponseDTO cancelTracking(CancelTrackingRequestDTO request, String userId);

    TrackingSessionResponseDTO getTrackingById(String trackingId);

    LiveTrackingDTO getLiveTracking(String trackingId);

    TrackingTimelineDTO getTimeline(String trackingId);

    PaginatedResponseDTO<TrackingSessionResponseDTO> getTrackingByUser(String userId, int page, int size, String sortBy, String sortDirection);

    PaginatedResponseDTO<TrackingSessionResponseDTO> getTrackingByDriver(String driverId, int page, int size);

    PaginatedResponseDTO<TrackingSessionResponseDTO> getAllTracking(int page, int size, String status, String sortBy, String sortDirection);
}