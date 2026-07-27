// dto/request/auth/GoogleAuthRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequestDTO {

    @NotBlank(message = "Google token is required")
    private String googleToken;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    private String picture;

    @NotBlank(message = "Google ID is required")
    private String googleId;
}