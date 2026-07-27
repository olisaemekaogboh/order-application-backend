// listener/ApplicationStartupListener.java
package com.inkfront.logisticsApplication.listener;

import com.inkfront.logisticsApplication.service.interfaces.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupListener {

    private final SystemConfigService systemConfigService;

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted(ApplicationStartedEvent event) {
        log.info("Application has started");

        try {
            // Load system configurations into cache
            systemConfigService.loadAllConfigsIntoCache();
            log.info("System configurations loaded into cache");
        } catch (Exception e) {
            log.error("Failed to load system configurations: {}", e.getMessage(), e);
        }
    }
}