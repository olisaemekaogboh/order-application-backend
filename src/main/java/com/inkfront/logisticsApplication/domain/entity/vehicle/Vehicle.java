package com.inkfront.logisticsApplication.domain.entity.vehicle;

import com.inkfront.logisticsApplication.domain.entity.BaseEntity;
import com.inkfront.logisticsApplication.domain.enums.FuelType;
import com.inkfront.logisticsApplication.domain.enums.TransmissionType;
import com.inkfront.logisticsApplication.domain.enums.VehicleStatus;
import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {

    @Column(name = "vehicle_number", unique = true, nullable = false)
    private String vehicleNumber; // internal ID

    @Column(name = "registration_number", unique = true, nullable = false)
    private String registrationNumber;

    @Column(name = "plate_number", unique = true)
    private String plateNumber;

    @Column(name = "vin", unique = true)
    private String vin;

    @Column(name = "engine_number")
    private String engineNumber;

    @Column(name = "chassis_number")
    private String chassisNumber;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "year")
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type")
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission")
    private TransmissionType transmission;

    @Column(name = "color")
    private String color;

    @Column(name = "capacity_kg")
    private Double capacityKg;

    @Column(name = "capacity_volume")
    private Double capacityVolume;

    @Column(name = "max_passengers")
    private Integer maxPassengers;

    @Column(name = "current_mileage")
    private Double currentMileage = 0.0;

    @Column(name = "fuel_consumption")
    private Double fuelConsumption; // km per liter

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @Column(name = "road_worthiness_expiry")
    private LocalDate roadWorthinessExpiry;

    @Column(name = "license_expiry")
    private LocalDate licenseExpiry;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_price")
    private Double purchasePrice;

    @Column(name = "last_inspection_date")
    private LocalDate lastInspectionDate;

    @Column(name = "next_inspection_date")
    private LocalDate nextInspectionDate;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 2026L;

    // Relationships
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleAssignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleMaintenance> maintenances = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleInspection> inspections = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleDocument> documents = new ArrayList<>();

    // Helper methods
    public boolean isAvailable() {
        return status == VehicleStatus.AVAILABLE;
    }

    public boolean isAssignable() {
        return status == VehicleStatus.AVAILABLE || status == VehicleStatus.INSPECTION_DUE;
    }

    public void assign() {
        this.status = VehicleStatus.ASSIGNED;
    }

    public void release() {
        this.status = VehicleStatus.AVAILABLE;
    }

    public void startMaintenance() {
        this.status = VehicleStatus.UNDER_MAINTENANCE;
    }

    public void completeMaintenance() {
        this.status = VehicleStatus.AVAILABLE;
        this.lastMaintenanceDate = LocalDate.now();
    }

    public void retire() {
        this.status = VehicleStatus.RETIRED;
        this.deleted = true;
    }
}