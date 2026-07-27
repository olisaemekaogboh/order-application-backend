package com.inkfront.logisticsApplication.domain.constants;


public class AppConstants {

    public static final String DEFAULT_CURRENCY = "NGN";
    public static final String DEFAULT_COUNTRY = "Nigeria";
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String DEFAULT_THEME = "light";

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int LOCK_TIME_MINUTES = 30;
    public static final int TOKEN_EXPIRY_MINUTES = 15;
    public static final int VERIFICATION_TOKEN_EXPIRY_HOURS = 24;

    public static final double MINIMUM_DISTANCE_KM = 1.0;
    public static final double MAXIMUM_DISTANCE_KM = 1000.0;
    public static final double MINIMUM_WEIGHT_KG = 0.1;
    public static final double MAXIMUM_WEIGHT_KG = 10000.0;
    public static final double MINIMUM_VOLUME_CUBIC = 0.01;
    public static final double MAXIMUM_VOLUME_CUBIC = 100.0;

    public static final String ORDER_NUMBER_PREFIX = "LOG-";
    public static final String TRANSACTION_PREFIX = "TXN-";
    public static final String REFERENCE_PREFIX = "REF-";

    public static final String CACHE_PREFIX = "logistics:";
    public static final String CACHE_USER = "users";
    public static final String CACHE_ORDER = "orders";
    public static final String CACHE_DRIVER = "drivers";
    public static final String CACHE_CONFIG = "configs";

    public static final String REDIS_USER_SESSION = "session:user:";
    public static final String REDIS_ORDER_LOCK = "lock:order:";
    public static final String REDIS_RATE_LIMIT = "rate:limit:";

    public static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/public/**",
            "/api/health",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health"
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
}