package com.inkfront.logisticsApplication.dto.request.dispatch;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchRequestDTO {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    private Integer priority;
    private LocalDateTime scheduledTime;
    private boolean autoAssign;
    private String notes;
}