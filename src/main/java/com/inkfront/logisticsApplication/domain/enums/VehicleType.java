package com.inkfront.logisticsApplication.domain.enums;


public enum VehicleType {
    MOTORCYCLE("Motorcycle"),
    MINI_VAN("Mini Van"),
    STANDARD("Standard"),
    TRUCK("Truck");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}