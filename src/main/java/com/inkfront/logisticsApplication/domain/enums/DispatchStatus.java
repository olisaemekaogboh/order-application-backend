package com.inkfront.logisticsApplication.domain.enums;

public enum DispatchStatus {
    PENDING("Pending"),
    WAITING_DRIVER_ACCEPTANCE("Waiting for Driver Acceptance"),
    DRIVER_ACCEPTED("Driver Accepted"),
    EN_ROUTE_PICKUP("En Route to Pickup"),
    PICKUP_COMPLETED("Pickup Completed"),
    DELIVERY_IN_PROGRESS("Delivery In Progress"),
    DELIVERED("Delivered"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    private final String displayName;

    DispatchStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == WAITING_DRIVER_ACCEPTANCE || this == DRIVER_ACCEPTED ||
                this == EN_ROUTE_PICKUP || this == PICKUP_COMPLETED ||
                this == DELIVERY_IN_PROGRESS;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean canBeReassigned() {
        return this == FAILED || this == WAITING_DRIVER_ACCEPTANCE;
    }
}