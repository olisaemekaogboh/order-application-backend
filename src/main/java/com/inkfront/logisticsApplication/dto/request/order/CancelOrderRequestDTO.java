package com.inkfront.logisticsApplication.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderRequestDTO {

    @NotBlank(message = "Cancellation reason is required")
    private String cancellationReason;
}