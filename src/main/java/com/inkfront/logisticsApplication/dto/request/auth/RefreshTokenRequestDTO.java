// dto/request/auth/RefreshTokenRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
