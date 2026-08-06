package com.inkfront.logisticsApplication.domain.entity.tracking;

import com.inkfront.logisticsApplication.domain.entity.BaseEntity;
import com.inkfront.logisticsApplication.domain.entity.User;
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
@Table(name = "tracking_events")
public class TrackingEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_session_id", nullable = false)
    private TrackingSession trackingSession;

    @Column(name = "event_type", nullable = false)
    private String eventType; // "STATUS_CHANGE", "CHECKPOINT"

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private TrackingStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private TrackingStatus newStatus;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}