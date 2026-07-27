// util/CookieUtils.java
package com.inkfront.logisticsApplication.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    private static final String COOKIE_PATH = "/";
    private static final boolean COOKIE_SECURE = false; // Set to true in production with HTTPS
    private static final boolean COOKIE_HTTP_ONLY = true;
    private static final String COOKIE_SAME_SITE = "Lax";

    public void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(COOKIE_SECURE);
        cookie.setHttpOnly(COOKIE_HTTP_ONLY);
        response.addCookie(cookie);
    }

    public void setTokenCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setSecure(COOKIE_SECURE);
        cookie.setHttpOnly(COOKIE_HTTP_ONLY);
        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);
        cookie.setSecure(COOKIE_SECURE);
        cookie.setHttpOnly(COOKIE_HTTP_ONLY);
        response.addCookie(cookie);
    }

    public String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public boolean hasCookie(HttpServletRequest request, String name) {
        return getCookieValue(request, name) != null;
    }

    public void clearAllCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                deleteCookie(response, cookie.getName());
            }
        }
    }
}