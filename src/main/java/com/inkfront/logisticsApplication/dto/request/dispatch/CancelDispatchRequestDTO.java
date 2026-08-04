package com.inkfront.logisticsApplication.dto.request.dispatch;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelDispatchRequestDTO {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}