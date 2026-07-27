package com.inkfront.logisticsApplication.service.interfaces;

import com.inkfront.logisticsApplication.dto.response.common.NotificationDTO;

public interface NotificationWebSocketService {

    void sendNotification(String userId, NotificationDTO notification);

    void broadcast(String destination, Object payload);
}