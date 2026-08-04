package com.inkfront.logisticsApplication.dto.request.dispatch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReassignDispatchRequestDTO {

    private String reason;

    private boolean autoAssign = false;

    private String specificDriverId;

    private String specificVehicleId;
}