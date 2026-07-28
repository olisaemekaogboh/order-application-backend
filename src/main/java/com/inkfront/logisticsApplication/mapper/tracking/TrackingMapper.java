package com.inkfront.logisticsApplication.mapper.tracking;

import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingEvent;
import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingLocation;
import com.inkfront.logisticsApplication.domain.entity.tracking.TrackingSession;
import com.inkfront.logisticsApplication.dto.response.tracking.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrackingMapper {

    // Mapping for TrackingSessionResponseDTO
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.name")
    TrackingSessionResponseDTO toResponseDTO(TrackingSession session);

    // Mapping for LiveTrackingDTO
    @Mapping(target = "trackingId", source = "id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "driverName", source = "driver.name")
    @Mapping(target = "driverPhone", source = "driver.phoneNumber")
    @Mapping(target = "latitude", source = "currentLatitude")
    @Mapping(target = "longitude", source = "currentLongitude")
    @Mapping(target = "lastUpdate", source = "lastUpdateTime")
    LiveTrackingDTO toLiveTrackingDTO(TrackingSession session);

    // Mapping for TrackingEventDTO
    @Mapping(target = "performedBy", source = "performedBy.fullName")
    TrackingEventDTO toEventDTO(TrackingEvent event);

    // Mapping for TrackingLocationDTO
    TrackingLocationDTO toLocationDTO(TrackingLocation location);

    // List mappings
    List<TrackingEventDTO> toEventDTOList(List<TrackingEvent> events);
    List<TrackingLocationDTO> toLocationDTOList(List<TrackingLocation> locations);
}