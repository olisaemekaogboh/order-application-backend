package com.inkfront.logisticsApplication.dto.response.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityDTO {

    private String userId;
    private String username;
    private Long totalActivities;
    private Map<String, Long> activityByAction;  // action -> count
    private Map<String, Long> activityByDay;     // date -> count
}