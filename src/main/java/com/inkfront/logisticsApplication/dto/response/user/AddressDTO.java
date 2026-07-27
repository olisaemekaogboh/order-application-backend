// dto/response/user/AddressDTO.java
package com.inkfront.logisticsApplication.dto.response.user;

import lombok.Data;

@Data
public class AddressDTO {

    private String id;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private boolean isDefault;
    private String label;
    private String landmark;
    private String recipientName;
    private String recipientPhone;
    private String fullAddress;
}