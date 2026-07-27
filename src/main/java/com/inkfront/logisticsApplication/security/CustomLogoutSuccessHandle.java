package com.inkfront.logisticsApplication.security;

import com.inkfront.logisticsApplication.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandle implements LogoutSuccessHandler {

    private final CookieUtils cookieUtils;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        log.info("Logout successful for user: {}",
                authentication != null ? authentication.getName() : "unknown");

        // Clear cookies
        cookieUtils.deleteCookie(response, "access_token");
        cookieUtils.deleteCookie(response, "refresh_token");

        // Clear security context
        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Logout successful\"}");
        response.getWriter().flush();
    }
}