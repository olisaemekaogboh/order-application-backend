// dto/request/user/UserUpdateRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequestDTO {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    private String profilePicture;
    private String preferredLanguage;
    private String themePreference;
}