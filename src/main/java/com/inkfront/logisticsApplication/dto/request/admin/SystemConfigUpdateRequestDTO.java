package com.inkfront.logisticsApplication.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfigUpdateRequestDTO {

    @NotBlank(message = "Configuration value is required")
    private String configValue;
}