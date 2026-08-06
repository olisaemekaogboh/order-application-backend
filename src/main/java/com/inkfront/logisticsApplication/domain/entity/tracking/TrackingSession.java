package com.inkfront.logisticsApplication.domain.entity.tracking;

import com.inkfront.logisticsApplication.domain.entity.BaseEntity;
import com.inkfront.logisticsApplication.domain.entity.Driver;
import com.inkfront.logisticsApplication.domain.entity.Order;
import com.inkfront.logisticsApplication.domain.enums.TrackingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(
        callSuper = true,
        onlyExplicitlyIncluded = true
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tracking_sessions")
public class TrackingSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TrackingStatus status = TrackingStatus.CREATED;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;

    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;

    @Column(name = "distance_traveled_km")
    private Double distanceTraveledKm = 0.0;

    @Column(name = "route_deviation_count")
    private Integer routeDeviationCount = 0;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}