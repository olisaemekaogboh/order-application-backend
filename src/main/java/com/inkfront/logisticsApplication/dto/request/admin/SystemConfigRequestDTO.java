package com.inkfront.logisticsApplication.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfigRequestDTO {

    @NotBlank(message = "Configuration key is required")
    private String configKey;

    @NotBlank(message = "Configuration value is required")
    private String configValue;

    private String category;

    private String description;
}