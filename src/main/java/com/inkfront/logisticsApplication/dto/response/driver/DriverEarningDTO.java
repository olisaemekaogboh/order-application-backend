// dto/response/driver/DriverEarningDTO.java
package com.inkfront.logisticsApplication.dto.response.driver;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DriverEarningDTO {

    private String id;
    private String driverId;
    private String driverName;
    private String orderId;
    private String orderNumber;
    private Double amount;
    private Double commission;
    private Double netAmount;
    private String currency;
    private LocalDateTime earningDate;
    private boolean paid;
    private LocalDateTime paidDate;
    private String paymentReference;
    private String notes;
    private Double commissionRate;

    private String formattedAmount;
    private String formattedNetAmount;
}