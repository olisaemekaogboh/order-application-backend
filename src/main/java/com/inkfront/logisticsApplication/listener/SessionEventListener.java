package com.inkfront.logisticsApplication.listener;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SessionEventListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        log.info("Session created: {}", event.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        log.info("Session destroyed: {}", event.getSession().getId());

        // Clean up any session-related resources
        String sessionId = event.getSession().getId();
        // Remove from active sessions map if maintained
    }
}