package com.inkfront.logisticsApplication.domain.enums;

public enum TrackingStatus {

    /**
     * Tracking session has been created.
     * Normally used internally before the driver accepts.
     */
    CREATED,

    /**
     * Driver has accepted the dispatch.
     */
    DRIVER_ACCEPTED,

    /**
     * Driver is travelling to pickup location.
     */
    DRIVER_EN_ROUTE_TO_PICKUP,

    /**
     * Driver has arrived at pickup location.
     */
    ARRIVED_PICKUP,

    /**
     * Parcel has been picked up.
     */
    PICKED_UP,

    /**
     * Driver is travelling to delivery destination.
     */
    IN_TRANSIT,

    /**
     * Driver has temporarily stopped.
     */
    STOPPED,

    /**
     * Route deviation detected.
     */
    ROUTE_DEVIATION,

    /**
     * Driver has arrived at delivery destination.
     */
    ARRIVED_DESTINATION,

    /**
     * Delivery completed.
     */
    DELIVERED,

    /**
     * Delivery failed.
     */
    FAILED,

    /**
     * Returning parcel.
     */
    RETURNING,

    /**
     * Parcel returned.
     */
    RETURNED,

    /**
     * Tracking cancelled.
     */
    CANCELLED
}