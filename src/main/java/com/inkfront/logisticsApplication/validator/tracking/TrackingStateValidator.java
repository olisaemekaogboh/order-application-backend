package com.inkfront.logisticsApplication.validator.tracking;

import com.inkfront.logisticsApplication.domain.enums.TrackingStatus;
import com.inkfront.logisticsApplication.exception.tracking.InvalidTrackingStateException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class TrackingStateValidator {

    private static final Map<TrackingStatus, Set<TrackingStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(TrackingStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(TrackingStatus.CREATED,
                EnumSet.of(TrackingStatus.ASSIGNED, TrackingStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TrackingStatus.ASSIGNED,
                EnumSet.of(TrackingStatus.DRIVER_ACCEPTED, TrackingStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TrackingStatus.DRIVER_ACCEPTED,
                EnumSet.of(TrackingStatus.DRIVER_EN_ROUTE_TO_PICKUP, TrackingStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TrackingStatus.DRIVER_EN_ROUTE_TO_PICKUP,
                EnumSet.of(TrackingStatus.ARRIVED_PICKUP, TrackingStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TrackingStatus.ARRIVED_PICKUP,
                EnumSet.of(TrackingStatus.PICKED_UP, TrackingStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TrackingStatus.PICKED_UP,
                EnumSet.of(TrackingStatus.IN_TRANSIT, TrackingStatus.CANCELLED, TrackingStatus.FAILED));
        ALLOWED_TRANSITIONS.put(TrackingStatus.IN_TRANSIT,
                EnumSet.of(TrackingStatus.STOPPED, TrackingStatus.ROUTE_DEVIATION, TrackingStatus.ARRIVED_DESTINATION,
                        TrackingStatus.CANCELLED, TrackingStatus.FAILED));
        ALLOWED_TRANSITIONS.put(TrackingStatus.STOPPED,
                EnumSet.of(TrackingStatus.IN_TRANSIT, TrackingStatus.CANCELLED, TrackingStatus.FAILED));
        ALLOWED_TRANSITIONS.put(TrackingStatus.ROUTE_DEVIATION,
                EnumSet.of(TrackingStatus.IN_TRANSIT, TrackingStatus.CANCELLED, TrackingStatus.FAILED));
        ALLOWED_TRANSITIONS.put(TrackingStatus.ARRIVED_DESTINATION,
                EnumSet.of(TrackingStatus.DELIVERED, TrackingStatus.FAILED, TrackingStatus.RETURNING));
        ALLOWED_TRANSITIONS.put(TrackingStatus.DELIVERED, EnumSet.noneOf(TrackingStatus.class));
        ALLOWED_TRANSITIONS.put(TrackingStatus.FAILED, EnumSet.noneOf(TrackingStatus.class));
        ALLOWED_TRANSITIONS.put(TrackingStatus.RETURNING,
                EnumSet.of(TrackingStatus.RETURNED, TrackingStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TrackingStatus.RETURNED, EnumSet.noneOf(TrackingStatus.class));
        ALLOWED_TRANSITIONS.put(TrackingStatus.CANCELLED, EnumSet.noneOf(TrackingStatus.class));
    }

    public void validateTransition(TrackingStatus from, TrackingStatus to) {
        Set<TrackingStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(TrackingStatus.class));
        if (!allowed.contains(to)) {
            throw new InvalidTrackingStateException(
                    String.format("Invalid status transition from %s to %s", from, to)
            );
        }
    }

    public boolean isTerminal(TrackingStatus status) {
        return status == TrackingStatus.DELIVERED ||
                status == TrackingStatus.FAILED ||
                status == TrackingStatus.RETURNED ||
                status == TrackingStatus.CANCELLED;
    }
}