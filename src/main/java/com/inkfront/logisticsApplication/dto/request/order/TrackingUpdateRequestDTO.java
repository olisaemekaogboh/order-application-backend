package com.inkfront.logisticsApplication.dto.request.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingUpdateRequestDTO {

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private String location;

    private String remarks;
}