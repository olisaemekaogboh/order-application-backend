package com.inkfront.logisticsApplication.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "flutterwave")
public class FlutterwaveProperties {
    private String secretKey;
    private String publicKey;
    private String encryptionKey;
    private String baseUrl = "https://api.flutterwave.com/v3";
    private String callbackUrl;
}