package com.inkfront.logisticsApplication.controller.auth;

import com.inkfront.logisticsApplication.domain.constants.SuccessMessages;
import com.inkfront.logisticsApplication.dto.request.auth.*;
import com.inkfront.logisticsApplication.dto.response.auth.AuthResponseDTO;
import com.inkfront.logisticsApplication.dto.response.common.ApiResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;
import com.inkfront.logisticsApplication.security.AuthenticatedUser;
import com.inkfront.logisticsApplication.service.interfaces.AuthService;
import com.inkfront.logisticsApplication.util.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private static final int REFRESH_TOKEN_COOKIE_AGE = 7 * 24 * 60 * 60;

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    /**
     * Login
     */
    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletResponse response) {

        log.info("Login request for email: {}", loginRequest.getEmail());

        AuthResponseDTO authResponse = authService.login(loginRequest);

        setAuthCookies(response, authResponse);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.LOGIN_SUCCESS,
                        authResponse
                )
        );
    }

    /**
     * Register
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO registerRequest) {

        log.info("Registration request for email: {}", registerRequest.getEmail());

        AuthResponseDTO authResponse = authService.register(registerRequest);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.REGISTRATION_SUCCESS,
                        authResponse
                )
        );
    }

    /**
     * Google Login
     */
    @PostMapping("/google")
    @Operation(summary = "Authenticate with Google")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> googleAuth(
            @Valid @RequestBody GoogleAuthRequestDTO request,
            HttpServletResponse response) {

        log.info("Google authentication request");

        AuthResponseDTO authResponse = authService.googleAuth(request);

        setAuthCookies(response, authResponse);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.LOGIN_SUCCESS,
                        authResponse
                )
        );
    }

    /**
     * Refresh Access Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO request,
            HttpServletResponse response) {

        log.info("Refresh token request");

        AuthResponseDTO authResponse = authService.refreshToken(request);

        setAuthCookies(response, authResponse);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Token refreshed successfully",
                        authResponse
                )
        );
    }

    /**
     * Logout
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<ApiResponseDTO<Void>> logout(
            Authentication authentication,
            HttpServletResponse response) {

        if (authentication != null) {
            authService.logout(authentication.getName());
        }

        cookieUtils.deleteCookie(response, "access_token");
        cookieUtils.deleteCookie(response, "refresh_token");

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.LOGOUT_SUCCESS,
                        null
                )
        );
    }

    /**
     * Forgot Password
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<ApiResponseDTO<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request) {

        log.info("Forgot password request for {}", request.getEmail());

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.PASSWORD_RESET_EMAIL_SENT,
                        null
                )
        );
    }

    /**
     * Reset Password
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password")
    public ResponseEntity<ApiResponseDTO<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request) {

        log.info("Password reset request");

        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.PASSWORD_RESET_SUCCESS,
                        null
                )
        );
    }

    /**
     * Verify Email
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email")
    public ResponseEntity<ApiResponseDTO<Void>> verifyEmail(
            @RequestParam String token) {

        log.info("Email verification requested");

        authService.verifyEmail(token);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.VERIFICATION_SUCCESS,
                        null
                )
        );
    }

    /**
     * Resend Verification Email
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email")
    public ResponseEntity<ApiResponseDTO<Void>> resendVerification(
            @RequestParam String email) {

        log.info("Resend verification email for {}", email);

        authService.resendVerificationEmail(email);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Verification email sent successfully.",
                        null
                )
        );
    }

    /**
     * Current User
     */
    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getCurrentUser(
            Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        log.info("Get current user request: {}", user.getEmail());

        UserDTO response =
                authService.getCurrentUser(user.getId());

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        SuccessMessages.DATA_RETRIEVED,
                        response
                )
        );
    }

    /**
     * Validate JWT
     */
    @GetMapping("/validate-token")
    @Operation(summary = "Validate JWT")
    public ResponseEntity<ApiResponseDTO<Boolean>> validateToken(
            @RequestParam String token) {

        boolean valid = authService.validateToken(token);

        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Token validation completed",
                        valid
                )
        );
    }

    /**
     * Helper
     */
    private void setAuthCookies(
            HttpServletResponse response,
            AuthResponseDTO authResponse) {

        cookieUtils.setTokenCookie(
                response,
                "access_token",
                authResponse.getAccessToken(),
                (int) (authResponse.getExpiresIn() / 1000)
        );

        cookieUtils.setTokenCookie(
                response,
                "refresh_token",
                authResponse.getRefreshToken(),
                REFRESH_TOKEN_COOKIE_AGE
        );
    }
}