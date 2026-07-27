package com.inkfront.logisticsApplication.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Skip JWT authentication for public endpoints.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return

                // Public authentication endpoints
                path.equals("/api/auth/login")
                        || path.equals("/api/auth/register")
                        || path.equals("/api/auth/google")
                        || path.equals("/api/auth/refresh")
                        || path.equals("/api/auth/forgot-password")
                        || path.equals("/api/auth/reset-password")
                        || path.equals("/api/auth/verify-email")
                        || path.equals("/api/auth/resend-verification")

                        // Swagger
                        || path.startsWith("/swagger-ui")
                        || path.startsWith("/v3/api-docs")
                        || path.startsWith("/webjars")

                        // Actuator
                        || path.startsWith("/actuator")

                        // Public API
                        || path.startsWith("/api/public")

                        // WebSocket
                        || path.startsWith("/ws");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String token = resolveToken(request);

            if (StringUtils.hasText(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtTokenProvider.validateToken(token)) {

                    String email = jwtTokenProvider.getEmailFromToken(token);
                    String userId = jwtTokenProvider.getUserIdFromToken(token);

                    UserDetails userDetails =
                            userDetailsService.loadUserByUsername(email);

                    AuthenticatedUser principal =
                            new AuthenticatedUser(
                                    userId,
                                    email,
                                    userDetails.getAuthorities()
                            );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    principal.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    request.setAttribute("userId", userId);
                    request.setAttribute("userEmail", email);

                    log.debug("Authenticated user: {}", email);
                }
            }

        } catch (Exception ex) {

            log.warn("JWT authentication skipped: {}", ex.getMessage());

            SecurityContextHolder.clearContext();

            /*
             * Never throw an exception here.
             *
             * Invalid JWTs should not stop public endpoints
             * like:
             *
             * /api/auth/login
             * /api/auth/google
             * /api/auth/register
             *
             * Protected endpoints will still be rejected
             * later by Spring Security because they remain
             * unauthenticated.
             */
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Resolve JWT from Authorization header or Cookie.
     */
    private String resolveToken(HttpServletRequest request) {

        String bearer = request.getHeader("Authorization");

        if (StringUtils.hasText(bearer)
                && bearer.startsWith("Bearer ")) {

            return bearer.substring(7);
        }

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {

            for (Cookie cookie : cookies) {

                if ("access_token".equals(cookie.getName())
                        && StringUtils.hasText(cookie.getValue())) {

                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}