package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.request.auth.GoogleAuthRequestDTO;
import com.inkfront.logisticsApplication.dto.response.user.UserDTO;

public interface GoogleAuthService {

    UserDTO authenticateWithGoogle(GoogleAuthRequestDTO googleAuthRequest);

    boolean verifyGoogleToken(String googleToken);

    UserDTO createOrUpdateUserFromGoogle(GoogleAuthRequestDTO googleAuthRequest);

    void disconnectGoogleAccount(String userId);
}