package com.inkfront.logisticsApplication.dto.response.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceResponseDTO {
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
}