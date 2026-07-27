// dto/response/common/NotificationDTO.java
package com.inkfront.logisticsApplication.dto.response.common;

import com.inkfront.logisticsApplication.domain.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {

    private String id;
    private String userId;
    private String title;
    private String message;
    private NotificationType type;
    private boolean read;
    private LocalDateTime readAt;
    private String relatedEntityId;
    private String relatedEntityType;
    private String actionUrl;
    private String imageUrl;
    private String priority;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private boolean delivered;
    private LocalDateTime deliveredAt;
}