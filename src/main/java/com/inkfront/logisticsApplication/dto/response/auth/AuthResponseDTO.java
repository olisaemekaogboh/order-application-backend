// dto/response/auth/AuthResponseDTO.java
package com.inkfront.logisticsApplication.dto.response.auth;

import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import lombok.Data;

@Data
public class AuthResponseDTO {

    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserDTO user;
    private boolean requiresTwoFactor = false;
}