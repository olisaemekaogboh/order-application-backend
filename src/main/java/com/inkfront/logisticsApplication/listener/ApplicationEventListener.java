// listener/ApplicationEventListener.java (Complete version)
package com.inkfront.logisticsApplication.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationEventListener {

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted(ApplicationStartedEvent event) {
        log.info("Application has started");
        // Application startup logic
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.info("Application is ready and fully initialized");

        // Initialize any required data
        // Start scheduled tasks
        // Warm up caches
        // Check system health
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationShutdown(ContextClosedEvent event) {
        log.info("Application is shutting down");

        // Clean up resources
        // Save pending data
        // Log shutdown
    }
}