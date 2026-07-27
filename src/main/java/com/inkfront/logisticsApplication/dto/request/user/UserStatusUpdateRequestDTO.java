package com.inkfront.logisticsApplication.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusUpdateRequestDTO {

    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}