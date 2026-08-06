package com.inkfront.logisticsApplication.service.impl.dispatch;

import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.domain.entity.User;
import com.inkfront.logisticsApplication.domain.enums.UserRole;
import com.inkfront.logisticsApplication.dto.response.dispatch.LiveDispatchDTO;
import com.inkfront.logisticsApplication.mapper.dispatch.DispatchMapper;
import com.inkfront.logisticsApplication.repository.DriverRepository;
import com.inkfront.logisticsApplication.repository.UserRepository;
import com.inkfront.logisticsApplication.repository.vehicle.VehicleRepository;
import com.inkfront.logisticsApplication.service.interfaces.NotificationService;
import com.inkfront.logisticsApplication.service.interfaces.dispatch.DispatchNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // Add this at class level to ensure all methods run in a transaction
public class DispatchNotificationServiceImpl implements DispatchNotificationService {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final DispatchMapper dispatchMapper;

    private void notifyAdminAndDispatchers(String title, String message) {
        try {
            List<User> adminUsers = userRepository.findByRole(UserRole.ADMIN);
            for (User user : adminUsers) {
                notificationService.sendSystemNotification(user.getId(), title, message);
            }
            List<User> dispatcherUsers = userRepository.findByRole(UserRole.DISPATCHER);
            for (User user : dispatcherUsers) {
                notificationService.sendSystemNotification(user.getId(), title, message);
            }
        } catch (Exception e) {
            log.warn("Failed to send role-based notifications: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true) // Read-only for query operations
    public void notifyDispatchCreated(Dispatch dispatch) {
        String message = "New dispatch created for order " + dispatch.getOrder().getOrderNumber();
        notifyAdminAndDispatchers("Dispatch Created", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    @Transactional(readOnly = true)
    public void notifyDispatchAssigned(Dispatch dispatch) {
        String message = "Dispatch assigned for order " + dispatch.getOrder().getOrderNumber();
        notifyAdminAndDispatchers("Dispatch Assigned", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    @Transactional(readOnly = true)
    public void notifyDriverAssigned(Dispatch dispatch, String driverName) {
        String userMessage = "You have been assigned to order " + dispatch.getOrder().getOrderNumber();

        // Send to driver
        if (dispatch.getDriverId() != null) {
            try {
                notificationService.sendSystemNotification(dispatch.getDriverId(), "New Assignment", userMessage);
            } catch (Exception e) {
                log.error("Error sending notification to driver {}: {}", dispatch.getDriverId(), e.getMessage());
            }
        }

        String dispatcherMessage = "Driver " + driverName + " assigned to order " + dispatch.getOrder().getOrderNumber();
        notifyAdminAndDispatchers("Driver Assigned", dispatcherMessage);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    @Transactional(readOnly = true)
    public void notifyVehicleAssigned(Dispatch dispatch, String vehicleNumber) {
        String message = "Vehicle " + vehicleNumber + " assigned to order " + dispatch.getOrder().getOrderNumber();
        notifyAdminAndDispatchers("Vehicle Assigned", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    @Transactional(readOnly = true)
    public void notifyDispatchAccepted(Dispatch dispatch) {
        String message = "Dispatch accepted for order " + dispatch.getOrder().getOrderNumber();

        // Send to customer
        if (dispatch.getOrder().getUser() != null) {
            try {
                notificationService.sendSystemNotification(
                        dispatch.getOrder().getUser().getId(),
                        "Dispatch Accepted",
                        message
                );
            } catch (Exception e) {
                log.error("Error sending notification to customer: {}", e.getMessage());
            }
        }

        notifyAdminAndDispatchers("Dispatch Accepted", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    @Transactional(readOnly = true)
    public void notifyDispatchRejected(Dispatch dispatch, String reason) {
        String message = "Dispatch rejected for order " + dispatch.getOrder().getOrderNumber() + ". Reason: " + reason;
        notifyAdminAndDispatchers("Dispatch Rejected", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    @Transactional(readOnly = true)
    public void notifyDispatchCompleted(Dispatch dispatch) {
        String message = "Dispatch completed for order " + dispatch.getOrder().getOrderNumber();

        // Send to customer
        if (dispatch.getOrder().getUser() != null) {
            try {
                notificationService.sendSystemNotification(
                        dispatch.getOrder().getUser().getId(),
                        "Order Completed",
                        message
                );
            } catch (Exception e) {
                log.warn("Failed to send notification to customer: {}", e.getMessage());
            }
        }

        // Send to driver - catch any errors
        if (dispatch.getDriverId() != null) {
            try {
                notificationService.sendSystemNotification(
                        dispatch.getDriverId(),
                        "Delivery Completed",
                        message
                );
            } catch (Exception e) {
                log.warn("Failed to send notification to driver {}: {}", dispatch.getDriverId(), e.getMessage());
            }
        }

        // Send to ADMIN and DISPATCHER roles
        try {
            notifyAdminAndDispatchers("Dispatch Completed", message);
        } catch (Exception e) {
            log.warn("Failed to send role notifications: {}", e.getMessage());
        }

        try {
            sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
        } catch (Exception e) {
            log.warn("Failed to send live update: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void notifyDispatchCancelled(Dispatch dispatch, String reason) {
        String message = "Dispatch cancelled for order " + dispatch.getOrder().getOrderNumber() + ". Reason: " + reason;

        // Send to customer
        if (dispatch.getOrder().getUser() != null) {
            try {
                notificationService.sendSystemNotification(
                        dispatch.getOrder().getUser().getId(),
                        "Dispatch Cancelled",
                        message
                );
            } catch (Exception e) {
                log.error("Error sending notification to customer: {}", e.getMessage());
            }
        }

        notifyAdminAndDispatchers("Dispatch Cancelled", message);
        sendLiveUpdate(dispatch.getId(), buildLiveDTO(dispatch));
    }

    @Override
    public void sendLiveUpdate(String dispatchId, LiveDispatchDTO update) {
        try {
            messagingTemplate.convertAndSend("/topic/dispatch/" + dispatchId, update);
        } catch (Exception e) {
            log.error("Error sending live update for dispatch {}: {}", dispatchId, e.getMessage());
        }
    }

    @Override
    public void sendToUser(String userId, LiveDispatchDTO update) {
        try {
            messagingTemplate.convertAndSendToUser(userId, "/queue/dispatch", update);
        } catch (Exception e) {
            log.error("Error sending live update to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Builds LiveDispatchDTO with properly fetched data.
     * Uses repositories to fetch data directly to avoid LazyInitializationException.
     */
    private LiveDispatchDTO buildLiveDTO(Dispatch dispatch) {
        LiveDispatchDTO dto = LiveDispatchDTO.builder()
                .dispatchId(dispatch.getId())
                .orderNumber(dispatch.getOrder().getOrderNumber())
                .status(dispatch.getStatus())
                .build();

        if (dispatch.getDriverId() != null) {
            // Use the repository to fetch the driver with its user data
            driverRepository.findById(dispatch.getDriverId()).ifPresent(driver -> {
                // Now fetch the user directly to ensure we have the data
                userRepository.findById(driver.getId()).ifPresent(user -> {
                    dto.setDriverName(user.getFullName());
                    dto.setDriverPhone(user.getPhoneNumber());
                });
                // Fallback to lazy loading if user not found (should not happen)
                if (dto.getDriverName() == null && driver.getUser() != null) {
                    dto.setDriverName(driver.getUser().getFullName());
                    dto.setDriverPhone(driver.getUser().getPhoneNumber());
                }
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