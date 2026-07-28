package com.inkfront.logisticsApplication.domain.enums;

public enum TrackingStatus {
    CREATED,
    ASSIGNED,
    DRIVER_ACCEPTED,
    DRIVER_EN_ROUTE_TO_PICKUP,
    ARRIVED_PICKUP,
    PICKED_UP,
    IN_TRANSIT,
    STOPPED,
    ROUTE_DEVIATION,
    ARRIVED_DESTINATION,
    DELIVERED,
    FAILED,
    RETURNING,
    RETURNED,
    CANCELLED
}