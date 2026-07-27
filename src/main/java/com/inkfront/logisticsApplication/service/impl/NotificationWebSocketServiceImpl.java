package com.inkfront.logisticsApplication.service.impl;

import com.inkfront.logisticsApplication.dto.response.common.NotificationDTO;
import com.inkfront.logisticsApplication.service.interfaces.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationWebSocketServiceImpl
        implements NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotification(String userId,
                                 NotificationDTO notification) {

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                notification
        );
    }

    @Override
    public void broadcast(String destination,
                          Object payload) {

        messagingTemplate.convertAndSend(destination, payload);
    }
}