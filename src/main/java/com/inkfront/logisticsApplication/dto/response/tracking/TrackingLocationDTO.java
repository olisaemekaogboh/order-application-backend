package com.inkfront.logisticsApplication.dto.response.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingLocationDTO {

    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private Double altitude;
    private Double bearing;
    private Double speed;
    private String provider;
    private LocalDateTime timestamp;
}