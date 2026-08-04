package com.inkfront.logisticsApplication.validator.dispatch;

import com.inkfront.logisticsApplication.domain.enums.DispatchStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

@Component
public class DispatchStateValidator {

    private static final EnumMap<DispatchStatus, Set<DispatchStatus>> VALID_TRANSITIONS = new EnumMap<>(DispatchStatus.class);

    static {
        // PENDING can go to:
        VALID_TRANSITIONS.put(DispatchStatus.PENDING,
                Set.of(
                        DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                        DispatchStatus.CANCELLED
                ));

        // WAITING_DRIVER_ACCEPTANCE can go to:
        VALID_TRANSITIONS.put(DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                Set.of(
                        DispatchStatus.DRIVER_ACCEPTED,
                        DispatchStatus.FAILED,
                        DispatchStatus.CANCELLED
                ));

        // DRIVER_ACCEPTED can go to:
        VALID_TRANSITIONS.put(DispatchStatus.DRIVER_ACCEPTED,
                Set.of(
                        DispatchStatus.EN_ROUTE_PICKUP,
                        DispatchStatus.FAILED,
                        DispatchStatus.CANCELLED
                ));

        // EN_ROUTE_PICKUP can go to:
        VALID_TRANSITIONS.put(DispatchStatus.EN_ROUTE_PICKUP,
                Set.of(
                        DispatchStatus.PICKUP_COMPLETED,
                        DispatchStatus.FAILED,
                        DispatchStatus.CANCELLED
                ));

        // PICKUP_COMPLETED can go to:
        VALID_TRANSITIONS.put(DispatchStatus.PICKUP_COMPLETED,
                Set.of(
                        DispatchStatus.DELIVERY_IN_PROGRESS,
                        DispatchStatus.FAILED,
                        DispatchStatus.CANCELLED
                ));

        // DELIVERY_IN_PROGRESS can go to:
        VALID_TRANSITIONS.put(DispatchStatus.DELIVERY_IN_PROGRESS,
                Set.of(
                        DispatchStatus.DELIVERED,
                        DispatchStatus.FAILED,
                        DispatchStatus.CANCELLED
                ));

        // DELIVERED is terminal
        VALID_TRANSITIONS.put(DispatchStatus.DELIVERED, new HashSet<>());

        // FAILED can go to:
        VALID_TRANSITIONS.put(DispatchStatus.FAILED,
                Set.of(
                        DispatchStatus.PENDING, // For retry
                        DispatchStatus.CANCELLED
                ));

        // CANCELLED is terminal
        VALID_TRANSITIONS.put(DispatchStatus.CANCELLED, new HashSet<>());
    }

    public boolean isValidTransition(DispatchStatus currentStatus, DispatchStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        Set<DispatchStatus> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);
        if (allowedTransitions == null) {
            return false;
        }

        return allowedTransitions.contains(newStatus);
    }

    public Set<DispatchStatus> getValidNextStates(DispatchStatus currentStatus) {
        return VALID_TRANSITIONS.getOrDefault(currentStatus, new HashSet<>());
    }

    public boolean isTerminalState(DispatchStatus status) {
        return status == DispatchStatus.DELIVERED || status == DispatchStatus.CANCELLED;
    }

    public boolean isActiveState(DispatchStatus status) {
        return status == DispatchStatus.WAITING_DRIVER_ACCEPTANCE ||
                status == DispatchStatus.DRIVER_ACCEPTED ||
                status == DispatchStatus.EN_ROUTE_PICKUP ||
                status == DispatchStatus.PICKUP_COMPLETED ||
                status == DispatchStatus.DELIVERY_IN_PROGRESS;
    }

    public boolean isDispatchable(DispatchStatus status) {
        return status == DispatchStatus.PENDING;
    }
}