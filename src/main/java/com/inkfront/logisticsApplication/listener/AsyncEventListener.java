package com.inkfront.logisticsApplication.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class AsyncEventListener {

    @Async
    @TransactionalEventListener
    public void handleAsyncEvent(String event) {
        // Handle event asynchronously
        log.info("Processing async event: {}", event);

        // Simulate processing time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Async event processed: {}", event);
    }
}