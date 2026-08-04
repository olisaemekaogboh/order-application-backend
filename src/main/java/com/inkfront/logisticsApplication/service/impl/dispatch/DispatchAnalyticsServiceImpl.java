package com.inkfront.logisticsApplication.service.impl.dispatch;

import com.inkfront.logisticsApplication.domain.enums.DispatchStatus;
import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchAnalyticsDTO;
import com.inkfront.logisticsApplication.repository.dispatch.DispatchRepository;
import com.inkfront.logisticsApplication.service.interfaces.dispatch.DispatchAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchAnalyticsServiceImpl implements DispatchAnalyticsService {

    private final DispatchRepository dispatchRepository;

    @Override
    public DispatchAnalyticsDTO getAnalytics() {
        long total = dispatchRepository.count();

        // Use only the statuses that exist in DispatchStatus enum
        long pending = dispatchRepository.countByStatus(DispatchStatus.PENDING);
        long waitingAcceptance = dispatchRepository.countByStatus(DispatchStatus.WAITING_DRIVER_ACCEPTANCE);
        long accepted = dispatchRepository.countByStatus(DispatchStatus.DRIVER_ACCEPTED);
        long enRoute = dispatchRepository.countByStatus(DispatchStatus.EN_ROUTE_PICKUP);
        long pickup = dispatchRepository.countByStatus(DispatchStatus.PICKUP_COMPLETED);
        long inTransit = dispatchRepository.countByStatus(DispatchStatus.DELIVERY_IN_PROGRESS);
        long completed = dispatchRepository.countByStatus(DispatchStatus.DELIVERED);
        long failed = dispatchRepository.countByStatus(DispatchStatus.FAILED);
        long cancelled = dispatchRepository.countByStatus(DispatchStatus.CANCELLED);

        // Calculate assigned as sum of active statuses
        long assigned = waitingAcceptance + accepted + enRoute + pickup + inTransit;

        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("PENDING", pending);
        byStatus.put("WAITING_DRIVER_ACCEPTANCE", waitingAcceptance);
        byStatus.put("DRIVER_ACCEPTED", accepted);
        byStatus.put("EN_ROUTE_PICKUP", enRoute);
        byStatus.put("PICKUP_COMPLETED", pickup);
        byStatus.put("DELIVERY_IN_PROGRESS", inTransit);
        byStatus.put("DELIVERED", completed);
        byStatus.put("FAILED", failed);
        byStatus.put("CANCELLED", cancelled);

        double avgDispatchTime = getAverageDispatchTime();
        double acceptanceRate = getDriverAcceptanceRate();
        double successRate = getDispatchSuccessRate();

        DispatchAnalyticsDTO dto = new DispatchAnalyticsDTO();
        dto.setTotalDispatches(total);
        dto.setPending(pending);
        dto.setAssigned(assigned);
        dto.setAccepted(accepted);
        dto.setCompleted(completed);
        dto.setCancelled(cancelled);
        dto.setFailed(failed);
        dto.setAverageDispatchTimeMinutes(avgDispatchTime);
        dto.setDriverAcceptanceRate(acceptanceRate);
        dto.setSuccessRate(successRate);
        dto.setDispatchesByStatus(byStatus);
        return dto;
    }

    @Override
    public DispatchAnalyticsDTO getAnalyticsForDateRange(LocalDate startDate, LocalDate endDate) {
        // simplified – just return getAnalytics for now
        return getAnalytics();
    }

    @Override
    public double getAverageDispatchTime() {
        Double avg = dispatchRepository.averageDispatchCompletionTime();
        return avg != null ? avg : 0.0;
    }

    @Override
    public double getDriverAcceptanceRate() {
        long totalAssigned = dispatchRepository.countByStatus(DispatchStatus.WAITING_DRIVER_ACCEPTANCE) +
                dispatchRepository.countByStatus(DispatchStatus.DRIVER_ACCEPTED);
        long accepted = dispatchRepository.countByStatus(DispatchStatus.DRIVER_ACCEPTED);
        return totalAssigned > 0 ? (double) accepted / totalAssigned * 100 : 0.0;
    }

    @Override
    public double getDispatchSuccessRate() {
        long total = dispatchRepository.count();
        long completed = dispatchRepository.countByStatus(DispatchStatus.DELIVERED);
        return total > 0 ? (double) completed / total * 100 : 0.0;
    }

    @Override
    public Map<String, Long> getDispatchesGroupedByStatus() {
        return getAnalytics().getDispatchesByStatus();
    }

    @Override
    public Map<String, Long> getDispatchesGroupedByDate(LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }
}