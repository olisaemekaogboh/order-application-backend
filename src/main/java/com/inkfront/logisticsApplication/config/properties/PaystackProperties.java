package com.inkfront.logisticsApplication.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "paystack")
public class PaystackProperties {
    private String secretKey;
    private String publicKey;
    private String baseUrl = "https://api.paystack.co";
    private String callbackUrl;
}