package com.inkfront.logisticsApplication.service.impl.dispatch;

import com.inkfront.logisticsApplication.domain.entity.dispatch.Dispatch;
import com.inkfront.logisticsApplication.domain.enums.DispatchStatus;
import com.inkfront.logisticsApplication.dto.response.dispatch.DispatchSummaryDTO;
import com.inkfront.logisticsApplication.mapper.dispatch.DispatchMapper;
import com.inkfront.logisticsApplication.repository.dispatch.DispatchRepository;
import com.inkfront.logisticsApplication.service.interfaces.dispatch.DispatchQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchQueueServiceImpl implements DispatchQueueService {

    private final DispatchRepository dispatchRepository;
    private final DispatchMapper dispatchMapper;

    @Override
    public List<Dispatch> getPendingDispatches() {
        return dispatchRepository.findPendingDispatches();
    }

    @Override
    public List<DispatchSummaryDTO> getPendingDispatchSummaries() {
        return dispatchRepository.findPendingDispatchesOrderedByPriority()
                .stream()
                .map(dispatchMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Scheduled(fixedDelay = 60000) // every minute
    @Transactional
    public void processScheduledDispatches() {
        log.info("Processing scheduled dispatches");
        List<Dispatch> dueDispatches = dispatchRepository.findScheduledDispatchesDue(LocalDateTime.now());
        for (Dispatch dispatch : dueDispatches) {
            if (dispatch.getStatus() == DispatchStatus.PENDING) {
                log.info("Processing scheduled dispatch: {}", dispatch.getId());
                // Could trigger auto-assignment here
            }
        }
    }

    @Override
    @Transactional
    public void retryFailedDispatch(String dispatchId) {
        log.info("Retrying failed dispatch: {}", dispatchId);
        Dispatch dispatch = dispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new IllegalArgumentException("Dispatch not found"));

        if (dispatch.getRetryCount() >= 3) {
            log.warn("Dispatch {} exceeded maximum retry count", dispatchId);
            dispatch.setStatus(DispatchStatus.FAILED);
            dispatchRepository.save(dispatch);
            return;
        }

        dispatch.setStatus(DispatchStatus.PENDING);
        dispatch.setRetryCount(dispatch.getRetryCount() + 1);
        dispatchRepository.save(dispatch);
    }

    @Override
    public void addToQueue(Dispatch dispatch) {
        // Already persisted; just ensure status is PENDING
        if (dispatch.getStatus() != DispatchStatus.PENDING) {
            dispatch.setStatus(DispatchStatus.PENDING);
            dispatchRepository.save(dispatch);
        }
    }

    @Override
    public void removeFromQueue(Dispatch dispatch) {
        // Mark as cancelled or completed
        if (dispatch.getStatus() != DispatchStatus.CANCELLED && dispatch.getStatus() != DispatchStatus.DELIVERED) {
            dispatch.setStatus(DispatchStatus.CANCELLED);
            dispatchRepository.save(dispatch);
        }
    }

    @Override
    @Transactional
    public void updatePriority(String dispatchId, int priority) {
        Dispatch dispatch = dispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new IllegalArgumentException("Dispatch not found"));
        dispatch.setPriority(priority);
        dispatchRepository.save(dispatch);
        log.info("Updated priority for dispatch {} to {}", dispatchId, priority);
    }
}