package com.inkfront.logisticsApplication.validator.dispatch;

import com.inkfront.logisticsApplication.domain.enums.DispatchStatus;
import com.inkfront.logisticsApplication.exception.dispatch.DispatchStateException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class DispatchStateValidator {

    private static final Map<DispatchStatus, Set<DispatchStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(DispatchStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(DispatchStatus.PENDING,
                EnumSet.of(DispatchStatus.SEARCHING_DRIVER, DispatchStatus.SEARCHING_VEHICLE, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.SEARCHING_DRIVER,
                EnumSet.of(DispatchStatus.DRIVER_ASSIGNED, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.SEARCHING_VEHICLE,
                EnumSet.of(DispatchStatus.VEHICLE_ASSIGNED, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.DRIVER_ASSIGNED,
                EnumSet.of(DispatchStatus.WAITING_DRIVER_ACCEPTANCE, DispatchStatus.REASSIGNED, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.VEHICLE_ASSIGNED,
                EnumSet.of(DispatchStatus.WAITING_DRIVER_ACCEPTANCE, DispatchStatus.REASSIGNED, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.WAITING_DRIVER_ACCEPTANCE,
                EnumSet.of(DispatchStatus.DRIVER_ACCEPTED, DispatchStatus.DRIVER_REJECTED, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.DRIVER_ACCEPTED,
                EnumSet.of(DispatchStatus.EN_ROUTE_PICKUP, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.DRIVER_REJECTED,
                EnumSet.of(DispatchStatus.PENDING, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.EN_ROUTE_PICKUP,
                EnumSet.of(DispatchStatus.PICKUP_COMPLETED, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.PICKUP_COMPLETED,
                EnumSet.of(DispatchStatus.DELIVERY_IN_PROGRESS, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.DELIVERY_IN_PROGRESS,
                EnumSet.of(DispatchStatus.DELIVERED, DispatchStatus.FAILED, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.DELIVERED, EnumSet.noneOf(DispatchStatus.class));
        ALLOWED_TRANSITIONS.put(DispatchStatus.FAILED,
                EnumSet.of(DispatchStatus.PENDING, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.REASSIGNED,
                EnumSet.of(DispatchStatus.PENDING, DispatchStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DispatchStatus.CANCELLED, EnumSet.noneOf(DispatchStatus.class));
    }

    public void validateTransition(DispatchStatus from, DispatchStatus to) {
        Set<DispatchStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(DispatchStatus.class));
        if (!allowed.contains(to)) {
            throw new DispatchStateException("Invalid dispatch state transition from " + from + " to " + to);
        }
    }
}