// dto/response/user/UserDTO.java
package com.inkfront.logisticsApplication.dto.response.user;

import com.inkfront.logisticsApplication.domain.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String profilePicture;
    private UserRole role;
    private boolean enabled;
    private boolean googleAuth;
    private String preferredLanguage;
    private String themePreference;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private List<AddressDTO> addresses;
    private Integer totalOrders;
    private Double totalSpent;
}