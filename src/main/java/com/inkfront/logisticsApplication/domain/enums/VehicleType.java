package com.inkfront.logisticsApplication.domain.enums;

public enum VehicleType {
    MOTORCYCLE("Motorcycle"),
    TRICYCLE("Tricycle"),
    SEDAN("Sedan"),
    SUV("SUV"),
    PICKUP("Pickup"),
    VAN("Van"),
    MINI_TRUCK("Mini Truck"),
    TRUCK("Truck"),
    TRAILER("Trailer"),
    REFRIGERATED_TRUCK("Refrigerated Truck"),
    TANKER("Tanker");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}