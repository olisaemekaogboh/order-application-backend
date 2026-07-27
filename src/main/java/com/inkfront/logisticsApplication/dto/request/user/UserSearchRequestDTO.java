package com.inkfront.logisticsApplication.dto.request.user;

import com.inkfront.logisticsApplication.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchRequestDTO {

    private String keyword;
    private UserRole role;
    private Boolean enabled;
    private Integer page;
    private Integer size;
}