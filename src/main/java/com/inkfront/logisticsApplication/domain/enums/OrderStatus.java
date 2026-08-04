package com.inkfront.logisticsApplication.domain.enums;

public enum OrderStatus {
    PENDING("Pending"),
    PAYMENT_PENDING("Payment Pending"),
    PAID("Paid"),
    READY_FOR_DISPATCH("Ready for Dispatch"),
    DISPATCH("Dispatch"),
    PICKED_UP("Picked Up"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Delivered"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == PENDING || this == PAYMENT_PENDING || this == PAID ||
                this == READY_FOR_DISPATCH || this == DISPATCH ||
                this == PICKED_UP || this == IN_TRANSIT;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == COMPLETED || this == CANCELLED;
    }

    public boolean canBeCancelled() {
        return this != DELIVERED && this != COMPLETED && this != CANCELLED;
    }
}