package com.inkfront.logisticsApplication.dto.request.user;

import com.inkfront.logisticsApplication.domain.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleUpdateRequestDTO {

    @NotNull(message = "Role is required")
    private UserRole role;
}