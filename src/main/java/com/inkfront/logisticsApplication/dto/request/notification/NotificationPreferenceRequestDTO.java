package com.inkfront.logisticsApplication.dto.request.notification;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceRequestDTO {

    @NotNull(message = "Email enabled flag is required")
    private Boolean emailEnabled;

    @NotNull(message = "SMS enabled flag is required")
    private Boolean smsEnabled;

    @NotNull(message = "Push enabled flag is required")
    private Boolean pushEnabled;
}