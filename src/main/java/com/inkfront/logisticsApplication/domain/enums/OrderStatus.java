package com.inkfront.logisticsApplication.domain.enums;



public enum OrderStatus {
    PENDING("Pending"),
    ASSIGNED("Assigned to Driver"),
    PICKED_UP("Picked Up"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
