package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.auth.*;
import com.inkfront.logisticsApplication.dto.response.auth.AuthResponseDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;

public interface AuthService {

    AuthResponseDTO login(LoginRequestDTO loginRequest);

    AuthResponseDTO register(RegisterRequestDTO registerRequest);

    AuthResponseDTO googleAuth(GoogleAuthRequestDTO googleAuthRequest);

    AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequest);

    void logout(String userId);

    void forgotPassword(ForgotPasswordRequestDTO forgotPasswordRequest);

    void resetPassword(ResetPasswordRequestDTO resetPasswordRequest);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);

    UserDTO getCurrentUser(String userId);

    boolean validateToken(String token);
}