// dto/response/order/OrderTrackingDTO.java
package com.inkfront.logisticsApplication.dto.response.order;

import com.inkfront.logisticsApplication.domain.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderTrackingDTO {

    private String orderNumber;
    private OrderStatus status;
    private String statusDisplayName;
    private String pickupLocation;
    private String deliveryLocation;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime estimatedDeliveryDate;
    private List<TrackingUpdateDTO> trackingHistory;
    private String driverName;
    private String driverPhone;
    private String driverPhoto;

    @Data
    public static class TrackingUpdateDTO {
        private OrderStatus status;
        private String statusDisplayName;
        private LocalDateTime timestamp;
        private String location;
        private String description;
        private Double latitude;
        private Double longitude;
    }
}