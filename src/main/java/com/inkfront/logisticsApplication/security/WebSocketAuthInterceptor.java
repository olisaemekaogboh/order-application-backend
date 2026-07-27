package com.inkfront.logisticsApplication.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.debug("Processing STOMP CONNECT frame");

            // Try to get Authorization header
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            String token = null;

            if (authHeaders != null && !authHeaders.isEmpty()) {
                String bearerToken = authHeaders.get(0);
                if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                    token = bearerToken.substring(7);
                }
            }

            // If token is present, validate it
            if (StringUtils.hasText(token)) {
                try {
                    if (jwtTokenProvider.validateToken(token)) {
                        String email = jwtTokenProvider.getEmailFromToken(token);
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        accessor.setUser(authentication);
                        log.debug("WebSocket authenticated for user: {}", email);
                        return message;
                    } else {
                        log.warn("Invalid JWT token provided, allowing anonymous access");
                    }
                } catch (Exception e) {
                    log.warn("Error validating token: {}, allowing anonymous access", e.getMessage());
                }
            } else {
                log.debug("No Authorization header, allowing anonymous access");
            }

            // If we reach here, either no token or invalid token -> anonymous
            Authentication anonymousAuth = new UsernamePasswordAuthenticationToken(
                    "anonymous",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );
            accessor.setUser(anonymousAuth);
        }

        return message;
    }
}