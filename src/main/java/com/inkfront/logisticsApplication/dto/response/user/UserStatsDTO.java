package com.inkfront.logisticsApplication.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsDTO {
    private Long totalUsers;
    private Long activeUsers;
    private Long disabledUsers;
    private Long suspendedUsers;
    private Map<String, Long> rolesDistribution;
    private Long newUsersThisMonth;
    private Long activeUsersThisMonth;
}