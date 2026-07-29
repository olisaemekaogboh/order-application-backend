package com.inkfront.logisticsApplication.domain.entity.vehicle;

import com.inkfront.logisticsApplication.domain.entity.BaseEntity;
import com.inkfront.logisticsApplication.domain.enums.MaintenanceStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "vehicle_maintenances")
public class VehicleMaintenance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "maintenance_date")
    private LocalDate maintenanceDate;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    @Column(name = "type")
    private String type; // e.g., OIL_CHANGE, TYRE_REPLACEMENT, etc.

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "service_provider")
    private String serviceProvider;

    @Column(name = "odometer_reading")
    private Double odometerReading;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}