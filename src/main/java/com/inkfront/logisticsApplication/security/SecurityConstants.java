package com.inkfront.logisticsApplication.security;

public class SecurityConstants {

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/api/auth/register";
    public static final String LOGIN_URL = "/api/auth/login";
    public static final String REFRESH_TOKEN_URL = "/api/auth/refresh";
    public static final String FORGOT_PASSWORD_URL = "/api/auth/forgot-password";
    public static final String RESET_PASSWORD_URL = "/api/auth/reset-password";
    public static final String VERIFY_EMAIL_URL = "/api/auth/verify-email";

    public static final String ROLE_CLIENT = "CLIENT";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    public static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/public/**",
            "/actuator/health",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/webjars/**"
    };

    public static final String[] ADMIN_URLS = {
            "/api/admin/**",
            "/api/drivers/**",
            "/api/revenue/**"
    };

    public static final String[] SUPER_ADMIN_URLS = {
            "/api/super-admin/**",
            "/api/system/**",
            "/api/audit/**"
    };

    public static final String[] AUTHENTICATED_URLS = {
            "/api/orders/**",
            "/api/users/**",
            "/api/addresses/**",
            "/api/notifications/**"
    };
}