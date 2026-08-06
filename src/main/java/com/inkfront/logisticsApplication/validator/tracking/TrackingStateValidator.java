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

    private static final Map<TrackingStatus, Set<TrackingStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(TrackingStatus.class);

    static {

        /*
         * Tracking starts immediately after the driver accepts
         * the dispatch.
         */
        ALLOWED_TRANSITIONS.put(
                TrackingStatus.CREATED,
                EnumSet.of(
                        TrackingStatus.DRIVER_ACCEPTED,
                        TrackingStatus.CANCELLED
                )
        );

        /*
         * Driver accepted dispatch.
         */
        ALLOWED_TRANSITIONS.put(
                TrackingStatus.DRIVER_ACCEPTED,
                EnumSet.of(
                        TrackingStatus.DRIVER_EN_ROUTE_TO_PICKUP,
                        TrackingStatus.CANCELLED
                )
        );

        /*
         * Driver travelling to pickup.
         */
        ALLOWED_TRANSITIONS.put(
                TrackingStatus.DRIVER_EN_ROUTE_TO_PICKUP,
                EnumSet.of(
                        TrackingStatus.ARRIVED_PICKUP,
                        TrackingStatus.CANCELLED,
                        TrackingStatus.FAILED
                )
        );

        /*
         * Driver arrived pickup.
         */
        ALLOWED_TRANSITIONS.put(
                TrackingStatus.ARRIVED_PICKUP,
                EnumSet.of(
                        TrackingStatus.PICKED_UP,
                        TrackingStatus.CANCELLED,
                        TrackingStatus.FAILED
                )
        );

        /*
         * Parcel collected.
         */
        ALLOWED_TRANSITIONS.put(
                TrackingStatus.PICKED_UP,
                EnumSet.of(
                        TrackingStatus.IN_TRANSIT,
                        TrackingStatus.CANCELLED,
                        TrackingStatus.FAILED
                )
        );

        /*
         * On the road.
         */
        ALLOWED_TRANSITIONS.put(
                TrackingStatus.IN_TRANSIT,
                EnumSet.of(
                        TrackingStatus.STOPPED,
                        TrackingStatus.ROUTE_DEVIATION,
                        TrackingStatus.ARRIVED_DESTINATION,
                        TrackingStatus.CANCELLED,
                        TrackingStatus.FAILED
                )
        );

        /*
         * Vehicle temporarily stopped.
         */
        ALLOWED_TRANSITIONS.put(
                TrackingStatus.STOPPED,
                EnumSet.of(
                        TrackingStatus.IN_TRANSIT,
                        TrackingStatus.CANCELLED,
                        TrackingStatus.FAILED
                )
        );

        /*
         * Route deviation.
         */
        ALLOWED_TRANSITIONS.put(
                TrackingStatus.ROUTE_DEVIATION,
                EnumSet.of(
                        TrackingStatus.IN_TRANSIT,
                        TrackingStatus.CANCELLED,
                        TrackingStatus.FAILED
                )
        );

        /*
         * Destination reached.
         */
        ALLOWED_TRANSITIONS.put(
                TrackingStatus.ARRIVED_DESTINATION,
                EnumSet.of(
                        TrackingStatus.DELIVERED,
                        TrackingStatus.RETURNING,
                        TrackingStatus.FAILED
                )
        );

        /*
         * Returning package.
         */
        ALLOWED_TRANSITIONS.put(
                TrackingStatus.RETURNING,
                EnumSet.of(
                        TrackingStatus.RETURNED,
                        TrackingStatus.CANCELLED
                )
        );

        /*
         * Terminal states.
         */
        ALLOWED_TRANSITIONS.put(
                TrackingStatus.DELIVERED,
                EnumSet.noneOf(TrackingStatus.class)
        );

        ALLOWED_TRANSITIONS.put(
                TrackingStatus.FAILED,
                EnumSet.noneOf(TrackingStatus.class)
        );

        ALLOWED_TRANSITIONS.put(
                TrackingStatus.RETURNED,
                EnumSet.noneOf(TrackingStatus.class)
        );

        ALLOWED_TRANSITIONS.put(
                TrackingStatus.CANCELLED,
                EnumSet.noneOf(TrackingStatus.class)
        );
    }

    public void validateTransition(
            TrackingStatus from,
            TrackingStatus to
    ) {

        Set<TrackingStatus> allowed =
                ALLOWED_TRANSITIONS.getOrDefault(
                        from,
                        EnumSet.noneOf(TrackingStatus.class)
                );

        if (!allowed.contains(to)) {
            throw new InvalidTrackingStateException(
                    String.format(
                            "Invalid status transition from %s to %s",
                            from,
                            to
                    )
            );
        }
    }

    public boolean isTerminal(TrackingStatus status) {

        return status == TrackingStatus.DELIVERED
                || status == TrackingStatus.FAILED
                || status == TrackingStatus.RETURNED
                || status == TrackingStatus.CANCELLED;
    }
}