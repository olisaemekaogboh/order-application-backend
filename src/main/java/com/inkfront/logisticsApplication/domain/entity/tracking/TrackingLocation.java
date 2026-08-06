package com.inkfront.logisticsApplication.domain.entity.tracking;

import com.inkfront.logisticsApplication.domain.entity.BaseEntity;
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
@Table(name = "tracking_locations")
public class TrackingLocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_session_id", nullable = false)
    private TrackingSession trackingSession;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "accuracy")
    private Double accuracy;

    @Column(name = "altitude")
    private Double altitude;

    @Column(name = "bearing")
    private Double bearing;

    @Column(name = "speed")
    private Double speed;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "provider")
    private String provider;

    @Column(name = "battery_level")
    private Double batteryLevel;

    @Column(name = "network_type")
    private String networkType;

    @Column(name = "is_current")
    private boolean current = false;
}