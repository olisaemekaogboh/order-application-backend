// security/OAuth2AuthenticationSuccessHandler.java
package com.inkfront.logisticsApplication.security;

import com.inkfront.logisticsApplication.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final CookieUtils cookieUtils;

    @Value("${oauth2.redirect.uri:http://localhost:3000/auth/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        log.info(
                "OAuth2 authentication success for: {}",
                String.valueOf(oAuth2User.getAttribute("email"))
        );

        // Redirect to frontend
        getRedirectStrategy().sendRedirect(request, response, redirectUri + "?success=true");
    }
}