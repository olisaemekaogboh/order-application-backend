package com.inkfront.logisticsApplication.config.payment;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Data
@Component
@Slf4j
public class FlutterwaveProperties {

    @Value("${payment.flutterwave.base-url:https://api.flutterwave.com/v3}")
    private String baseUrl;

    @Value("${payment.flutterwave.secret-key:}")
    private String secretKey;

    @Value("${payment.flutterwave.public-key:}")
    private String publicKey;

    @Value("${payment.flutterwave.encryption-key:}")
    private String encryptionKey;

    @Value("${payment.flutterwave.callback-url:http://localhost:3000/payment/callback}")
    private String callbackUrl;

//    @PostConstruct
//    public void init() {
//        log.info("========================================");
//        log.info("🌊 FLUTTERWAVE PROPERTIES LOADED");
//        log.info("Base URL: {}", baseUrl);
//        log.info("Secret Key: {}", maskKey(secretKey));
//        log.info("Public Key: {}", maskKey(publicKey));
//        log.info("Encryption Key: {}", maskKey(encryptionKey));
//        log.info("========================================");
//    }

    private String maskKey(String key) {
        if (key == null) return "NULL";
        if (key.isEmpty()) return "EMPTY";
        if (key.length() <= 8) return "***";
        return key.substring(0, 6) + "..." + key.substring(key.length() - 4);
    }
}