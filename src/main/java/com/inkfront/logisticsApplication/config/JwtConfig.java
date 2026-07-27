package com.inkfront.logisticsApplication.config;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;
    private long expiration;
    private long refreshExpiration;
    private String issuer;
    private String audience;

    @Bean
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public long getAccessTokenExpiration() {
        return expiration * 1000; // Convert to milliseconds
    }

    public long getRefreshTokenExpiration() {
        return refreshExpiration * 1000; // Convert to milliseconds
    }
}