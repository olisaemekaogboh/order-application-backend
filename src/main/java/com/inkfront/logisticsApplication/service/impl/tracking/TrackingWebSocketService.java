package com.inkfront.logisticsApplication.service.impl.tracking;

import com.inkfront.logisticsApplication.dto.response.tracking.LiveTrackingDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendLiveUpdate(String trackingId, LiveTrackingDTO update) {
        try {
            messagingTemplate.convertAndSend("/topic/tracking/" + trackingId, update);
            log.debug("Sent live update to /topic/tracking/{}", trackingId);
        } catch (Exception e) {
            log.error("Failed to send live update for tracking {}: {}", trackingId, e.getMessage(), e);
        }
    }

    public void sendToUser(String userId, LiveTrackingDTO update) {
        try {
            messagingTemplate.convertAndSendToUser(userId, "/queue/tracking", update);
            log.debug("Sent live update to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send live update to user {}: {}", userId, e.getMessage(), e);
        }
    }
}