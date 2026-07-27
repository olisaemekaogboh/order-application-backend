package com.inkfront.logisticsApplication.security;

import com.inkfront.logisticsApplication.config.JwtConfig;
import com.inkfront.logisticsApplication.domain.entity.User;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    /**
     * Generate Access Token
     */
    public String generateAccessToken(User user) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getAccessTokenExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuer(jwtConfig.getIssuer())
                .setAudience(jwtConfig.getAudience())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate Refresh Token
     */
    public String generateRefreshToken(User user) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getRefreshTokenExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate Token
     */
    public boolean validateToken(String token) {

        try {

            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (ExpiredJwtException ex) {

            log.error("JWT token expired: {}", ex.getMessage());

        } catch (UnsupportedJwtException ex) {

            log.error("Unsupported JWT token: {}", ex.getMessage());

        } catch (MalformedJwtException ex) {

            log.error("Malformed JWT token: {}", ex.getMessage());

        } catch (SecurityException ex) {

            log.error("Invalid JWT signature: {}", ex.getMessage());

        } catch (IllegalArgumentException ex) {

            log.error("JWT claims string is empty: {}", ex.getMessage());

        }

        return false;
    }

    /**
     * Extract User ID
     */
    public String getUserIdFromToken(String token) {
        return getClaims(token).get("userId", String.class);
    }

    /**
     * Extract Email
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract Role
     */
    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Check Refresh Token
     */
    public boolean isRefreshToken(String token) {

        String type = getClaims(token).get("type", String.class);

        return "refresh".equals(type);
    }

    /**
     * Token Expiration
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * Token Expired?
     */
    public boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    /**
     * Access Token Lifetime
     */
    public long getAccessTokenExpiration() {
        return jwtConfig.getAccessTokenExpiration();
    }

    /**
     * Refresh Token Lifetime
     */
    public long getRefreshTokenExpiration() {
        return jwtConfig.getRefreshTokenExpiration();
    }

    /**
     * Read Claims
     */
    private Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}