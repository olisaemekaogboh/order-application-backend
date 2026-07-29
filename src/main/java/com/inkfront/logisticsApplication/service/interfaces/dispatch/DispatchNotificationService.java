package com.inkfront.logisticsApplication.service.interfaces.dispatch;

import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.dto.response.dispatch.LiveDispatchDTO;

public interface DispatchNotificationService {

    void notifyDispatchCreated(Dispatch dispatch);

    void notifyDispatchAssigned(Dispatch dispatch);

    void notifyDriverAssigned(Dispatch dispatch, String driverName);

    void notifyVehicleAssigned(Dispatch dispatch, String vehicleNumber);

    void notifyDispatchAccepted(Dispatch dispatch);

    void notifyDispatchRejected(Dispatch dispatch, String reason);

    void notifyDispatchCompleted(Dispatch dispatch);

    void notifyDispatchCancelled(Dispatch dispatch, String reason);

    void sendLiveUpdate(String dispatchId, LiveDispatchDTO update);

    void sendToUser(String userId, LiveDispatchDTO update);
}