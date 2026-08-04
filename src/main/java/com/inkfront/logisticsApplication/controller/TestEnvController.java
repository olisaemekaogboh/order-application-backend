package com.inkfront.logisticsApplication.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestEnvController {

    @Value("${payment.flutterwave.secret-key:NOT_LOADED}")
    private String secretKey;

    @Value("${payment.flutterwave.public-key:NOT_LOADED}")
    private String publicKey;

    @Value("${payment.provider:NOT_LOADED}")
    private String provider;

    @GetMapping("/env")
    public Map<String, String> testEnv() {
        Map<String, String> result = new HashMap<>();
        result.put("provider", provider);
        result.put("secretKey", secretKey.substring(0, Math.min(10, secretKey.length())) + "...");
        result.put("publicKey", publicKey.substring(0, Math.min(10, publicKey.length())) + "...");
        result.put("secretKeyLength", String.valueOf(secretKey.length()));
        result.put("isLoaded", secretKey.startsWith("FLWSECK_TEST-") ? "✅ YES" : "❌ NO");
        return result;
    }
}