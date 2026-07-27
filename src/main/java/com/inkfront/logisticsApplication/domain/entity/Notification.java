package com.inkfront.logisticsApplication.domain.entity;




import com.inkfront.logisticsApplication.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "is_read")
    private boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "related_entity_id")
    private String relatedEntityId;

    @Column(name = "related_entity_type")
    private String relatedEntityType;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "priority")
    private String priority = "NORMAL";

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered")
    private boolean delivered = false;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}
