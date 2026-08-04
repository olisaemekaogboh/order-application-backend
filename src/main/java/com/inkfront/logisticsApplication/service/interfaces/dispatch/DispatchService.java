package com.inkfront.logisticsApplication.service.interfaces.dispatch;

import com.inkfront.logisticsApplication.dto.request.dispatch.*;
import com.inkfront.logisticsApplication.dto.response.common.PaginatedResponseDTO;
import com.inkfront.logisticsApplication.dto.response.dispatch.*;

public interface DispatchService {

    DispatchResponseDTO createDispatch(DispatchRequestDTO request, String userId);

    // NEW: Manual assign with driver + vehicle + priority + notes
    DispatchResponseDTO manualAssignDispatch(ManualAssignDispatchRequestDTO request, String userId);

    DispatchResponseDTO assignDriver(String dispatchId, AssignDriverRequestDTO request, String userId);

    DispatchResponseDTO assignVehicle(String dispatchId, AssignVehicleRequestDTO request, String userId);

    DispatchResponseDTO acceptDispatch(String dispatchId, String userId);

    DispatchResponseDTO rejectDispatch(String dispatchId, String reason, String userId);

    DispatchResponseDTO reassignDispatch(String dispatchId, String userId);

    DispatchResponseDTO cancelDispatch(String dispatchId, String reason, String userId);

    DispatchResponseDTO completeDispatch(String dispatchId, String userId);

    DispatchResponseDTO getDispatchById(String dispatchId);

    DispatchResponseDTO getDispatchByOrder(String orderId);

    PaginatedResponseDTO<DispatchSummaryDTO> getDispatchesByDriver(String driverId, int page, int size);

    PaginatedResponseDTO<DispatchSummaryDTO> getDispatchesByVehicle(String vehicleId, int page, int size);

    PaginatedResponseDTO<DispatchSummaryDTO> getAllDispatches(int page, int size, String status, String sortBy, String sortDirection);

    DispatchAnalyticsDTO getDispatchAnalytics();
}