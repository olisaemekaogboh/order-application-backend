package com.inkfront.logisticsApplication.dto.request.notification;

import com.inkfront.logisticsApplication.domain.enums.NotificationType;
import com.inkfront.logisticsApplication.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastNotificationRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    private UserRole recipientRole;  // null = all users
}