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
public class NotificationReadRequestDTO {

    @NotNull(message = "Read status is required")
    private Boolean read;
}