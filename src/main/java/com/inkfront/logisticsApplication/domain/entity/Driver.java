package com.inkfront.logisticsApplication.domain.entity;




import com.inkfront.logisticsApplication.domain.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(
        callSuper = true,
        onlyExplicitlyIncluded = true
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "drivers")
public class Driver extends BaseEntity {


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

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "five_star_count")
    private Integer fiveStarCount = 0;

    @Column(name = "four_star_count")
    private Integer fourStarCount = 0;

    @Column(name = "three_star_count")
    private Integer threeStarCount = 0;

    @Column(name = "two_star_count")
    private Integer twoStarCount = 0;

    @Column(name = "one_star_count")
    private Integer oneStarCount = 0;

    @Column(name = "completed_orders")
    private Integer completedOrders = 0;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> assignedOrders = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

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