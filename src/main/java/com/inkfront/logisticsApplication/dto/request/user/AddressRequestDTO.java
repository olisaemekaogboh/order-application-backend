// dto/request/user/AddressRequestDTO.java
package com.inkfront.logisticsApplication.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequestDTO {

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    private String country = "Nigeria";
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private boolean isDefault = false;
    private String label;
    private String landmark;
    private String recipientName;
    private String recipientPhone;
}