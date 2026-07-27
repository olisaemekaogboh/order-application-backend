// dto/response/auth/JwtResponseDTO.java
package com.inkfront.logisticsApplication.dto.response.auth;

import lombok.Data;

@Data
public class JwtResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
}