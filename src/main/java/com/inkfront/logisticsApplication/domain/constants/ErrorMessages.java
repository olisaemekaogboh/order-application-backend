package com.inkfront.logisticsApplication.domain.constants;

public class ErrorMessages {

    // Authentication Errors
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String ACCOUNT_LOCKED = "Account is locked. Please try again later";
    public static final String ACCOUNT_DISABLED = "Account is disabled. Please contact support";
    public static final String UNAUTHORIZED = "Unauthorized access";
    public static final String FORBIDDEN = "Forbidden access";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String INVALID_TOKEN = "Invalid token";

    // User Errors
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String PHONE_ALREADY_EXISTS = "Phone number already exists";

    // Order Errors
    public static final String ORDER_NOT_FOUND = "Order not found";
    public static final String INVALID_ORDER_STATUS = "Invalid order status transition";
    public static final String ORDER_ALREADY_DELIVERED = "Order already delivered";
    public static final String ORDER_ALREADY_CANCELLED = "Order already cancelled";
    public static final String CANNOT_CANCEL_ORDER = "Cannot cancel order in current status";

    // Driver Errors
    public static final String DRIVER_NOT_FOUND = "Driver not found";
    public static final String DRIVER_NOT_AVAILABLE = "Driver is not available";
    public static final String DRIVER_ALREADY_ASSIGNED = "Driver already assigned to another order";
    public static final String INVALID_VEHICLE_TYPE = "Invalid vehicle type";

    // Payment Errors
    public static final String PAYMENT_FAILED = "Payment failed";
    public static final String INVALID_PAYMENT = "Invalid payment";
    public static final String PAYMENT_NOT_FOUND = "Payment not found";

    // Validation Errors
    public static final String INVALID_DISTANCE = "Invalid distance value";
    public static final String INVALID_WEIGHT = "Invalid weight value";
    public static final String INVALID_VOLUME = "Invalid volume value";
    public static final String INVALID_ADDRESS = "Invalid address";

    // General Errors
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String BAD_REQUEST = "Bad request";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String DUPLICATE_RESOURCE = "Duplicate resource";
}
