package com.inkfront.logisticsApplication.service.interfaces.dispatch;

import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchSummaryDTO;
import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;

import java.util.List;

public interface DispatchQueueService {

    List<Dispatch> getPendingDispatches();

    List<DispatchSummaryDTO> getPendingDispatchSummaries();

    void processScheduledDispatches();

    void retryFailedDispatch(String dispatchId);

    void addToQueue(Dispatch dispatch);

    void removeFromQueue(Dispatch dispatch);

    void updatePriority(String dispatchId, int priority);
}