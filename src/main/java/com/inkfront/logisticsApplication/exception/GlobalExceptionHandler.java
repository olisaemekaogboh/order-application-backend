package com.inkfront.logisticsApplication.exception;

import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.domain.constants.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ex.getMessage(),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBadRequest(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ex.getMessage(),
                "BAD_REQUEST",
                HttpStatus.BAD_REQUEST.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUnauthorized(UnauthorizedException ex) {
        log.error("Unauthorized: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ex.getMessage(),
                "UNAUTHORIZED",
                HttpStatus.UNAUTHORIZED.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateResource(DuplicateResourceException ex) {
        log.error("Duplicate resource: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ex.getMessage(),
                "DUPLICATE_RESOURCE",
                HttpStatus.CONFLICT.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handlePaymentError(PaymentException ex) {
        log.error("Payment error: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ex.getMessage(),
                "PAYMENT_ERROR",
                HttpStatus.PAYMENT_REQUIRED.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleValidation(ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.UNPROCESSABLE_ENTITY.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponseDTO<Map<String, String>> response = ApiResponseDTO.error(
                "Validation failed",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        response.setData(errors);
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (error1, error2) -> error1 + ", " + error2
                ));

        ApiResponseDTO<Map<String, String>> response = ApiResponseDTO.error(
                "Validation failed",
                "CONSTRAINT_VIOLATION",
                HttpStatus.BAD_REQUEST.value()
        );
        response.setData(errors);
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ErrorMessages.FORBIDDEN,
                "ACCESS_DENIED",
                HttpStatus.FORBIDDEN.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAuthentication(AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ErrorMessages.INVALID_CREDENTIALS,
                "AUTHENTICATION_ERROR",
                HttpStatus.UNAUTHORIZED.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.error("Bad credentials: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ErrorMessages.INVALID_CREDENTIALS,
                "BAD_CREDENTIALS",
                HttpStatus.UNAUTHORIZED.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch: {}", ex.getMessage());
        String message = String.format("Parameter '%s' has invalid value: %s",
                ex.getName(), ex.getValue());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                message,
                "TYPE_MISMATCH",
                HttpStatus.BAD_REQUEST.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMissingParameter(
            MissingServletRequestParameterException ex) {
        log.error("Missing parameter: {}", ex.getMessage());
        String message = String.format("Required parameter '%s' of type '%s' is missing",
                ex.getParameterName(), ex.getParameterType());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                message,
                "MISSING_PARAMETER",
                HttpStatus.BAD_REQUEST.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        log.error("Message not readable: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                "Malformed JSON request",
                "MALFORMED_JSON",
                HttpStatus.BAD_REQUEST.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex) {
        log.error("Max upload size exceeded: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                "File size exceeds maximum allowed limit",
                "FILE_TOO_LARGE",
                HttpStatus.PAYLOAD_TOO_LARGE.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ex.getMessage(),
                "ILLEGAL_ARGUMENT",
                HttpStatus.BAD_REQUEST.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalState(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ex.getMessage(),
                "ILLEGAL_STATE",
                HttpStatus.CONFLICT.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalAccess(IllegalAccessException ex) {
        log.error("Illegal access: {}", ex.getMessage());
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ex.getMessage(),
                "ILLEGAL_ACCESS",
                HttpStatus.FORBIDDEN.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ErrorMessages.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: ", ex);
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ex.getMessage() != null ? ex.getMessage() : ErrorMessages.INTERNAL_SERVER_ERROR,
                "RUNTIME_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleThrowable(Throwable ex) {
        log.error("Unexpected throwable: ", ex);
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                ErrorMessages.INTERNAL_SERVER_ERROR,
                "UNEXPECTED_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}