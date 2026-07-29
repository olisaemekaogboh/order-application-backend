package com.inkfront.logisticsApplication.service.impl.dispatch;

import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.dto.response.dispatch.LiveDispatchDTO;
import com.inkfront.logisticsApplication.mapper.dispatch.DispatchMapper;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.dispatch.DispatchNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchNotificationServiceImpl implements DispatchNotificationService {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DispatchMapper dispatchMapper;

    @Override
    public void notifyDispatchCreated(Dispatch dispatch) {
        String message = "New dispatch created for order " + dispatch.getOrder().getOrderNumber();
        notificationService.sendSystemNotification("DISPATCHER", "Dispatch Created", message);
        // Broadcast to dispatchers
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    public void notifyDispatchAssigned(Dispatch dispatch) {
        String message = "Dispatch assigned for order " + dispatch.getOrder().getOrderNumber();
        notificationService.sendSystemNotification("DISPATCHER", "Dispatch Assigned", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    public void notifyDriverAssigned(Dispatch dispatch, String driverName) {
        String userMessage = "You have been assigned to order " + dispatch.getOrder().getOrderNumber();
        notificationService.sendSystemNotification(dispatch.getDriverId(), "New Assignment", userMessage);

        String dispatcherMessage = "Driver " + driverName + " assigned to order " + dispatch.getOrder().getOrderNumber();
        notificationService.sendSystemNotification("DISPATCHER", "Driver Assigned", dispatcherMessage);

        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    public void notifyVehicleAssigned(Dispatch dispatch, String vehicleNumber) {
        String message = "Vehicle " + vehicleNumber + " assigned to order " + dispatch.getOrder().getOrderNumber();
        notificationService.sendSystemNotification("DISPATCHER", "Vehicle Assigned", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    public void notifyDispatchAccepted(Dispatch dispatch) {
        String message = "Dispatch accepted for order " + dispatch.getOrder().getOrderNumber();
        notificationService.sendSystemNotification(dispatch.getOrder().getUser().getId(), "Dispatch Accepted", message);
        notificationService.sendSystemNotification("DISPATCHER", "Dispatch Accepted", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    public void notifyDispatchRejected(Dispatch dispatch, String reason) {
        String message = "Dispatch rejected for order " + dispatch.getOrder().getOrderNumber() + ". Reason: " + reason;
        notificationService.sendSystemNotification("DISPATCHER", "Dispatch Rejected", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    public void notifyDispatchCompleted(Dispatch dispatch) {
        String message = "Dispatch completed for order " + dispatch.getOrder().getOrderNumber();
        notificationService.sendSystemNotification(dispatch.getOrder().getUser().getId(), "Order Completed", message);
        notificationService.sendSystemNotification(dispatch.getDriverId(), "Delivery Completed", message);
        notificationService.sendSystemNotification("DISPATCHER", "Dispatch Completed", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    public void notifyDispatchCancelled(Dispatch dispatch, String reason) {
        String message = "Dispatch cancelled for order " + dispatch.getOrder().getOrderNumber() + ". Reason: " + reason;
        notificationService.sendSystemNotification(dispatch.getOrder().getUser().getId(), "Dispatch Cancelled", message);
        notificationService.sendSystemNotification("DISPATCHER", "Dispatch Cancelled", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    public void sendLiveUpdate(String dispatchId, LiveDispatchDTO update) {
        messagingTemplate.convertAndSend("/topic/dispatch/" + dispatchId, update);
    }

    @Override
    public void sendToUser(String userId, LiveDispatchDTO update) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/dispatch", update);
    }

    private LiveDispatchDTO buildLiveDTO(Dispatch dispatch) {
        LiveDispatchDTO dto = LiveDispatchDTO.builder()
                .dispatchId(dispatch.getId())
                .orderNumber(dispatch.getOrder().getOrderNumber())
                .status(dispatch.getStatus())
                .build();

        if (dispatch.getDriverId() != null) {
            driverRepository.findById(dispatch.getDriverId()).ifPresent(driver -> {
                dto.setDriverName(driver.getName());
                dto.setDriverPhone(driver.getPhoneNumber());
                dto.setDriverLatitude(driver.getCurrentLatitude());
                dto.setDriverLongitude(driver.getCurrentLongitude());
            });
        }

        if (dispatch.getVehicleId() != null) {
            vehicleRepository.findById(dispatch.getVehicleId()).ifPresent(vehicle ->
                    dto.setVehicleNumber(vehicle.getVehicleNumber()));
        }

        dto.setLastUpdate(java.time.LocalDateTime.now());
        return dto;
    }
}