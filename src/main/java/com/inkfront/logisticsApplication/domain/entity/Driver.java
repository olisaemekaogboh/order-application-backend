package com.inkfront.logisticsApplication.domain.entity;




import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "drivers")
public class Driver extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "vehicle_plate_number", nullable = false, unique = true)
    private String vehiclePlateNumber;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "available_balance")
    private Double availableBalance = 0.0;

    @Column(name = "available", nullable = false)
    private Boolean available = true;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Column(name = "current_location")
    private String currentLocation;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "total_earnings")
    private Double totalEarnings = 0.0;

    @Column(name = "completed_orders")
    private Integer completedOrders = 0;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> assignedOrders = new ArrayList<>();

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DriverEarning> earnings = new ArrayList<>();

    public void updateRating(double newRating) {
        if (this.rating == null) {
            this.rating = 0.0;
        }
        this.rating = (this.rating + newRating) / 2;
    }

    public void addEarnings(double amount) {

        if (availableBalance == null)
            availableBalance = 0.0;

        if (totalEarnings == null)
            totalEarnings = 0.0;

        if (completedOrders == null)
            completedOrders = 0;

        availableBalance += amount;
        totalEarnings += amount;
        completedOrders++;
    }

}