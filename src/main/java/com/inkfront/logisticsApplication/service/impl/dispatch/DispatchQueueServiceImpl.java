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
    private static final int MAX_RETRIES = 3;

    @Override
    public List<Dispatch> getPendingDispatches() {
        return dispatchRepository.findPendingDispatches();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispatchSummaryDTO> getPendingDispatchSummaries() {

        return dispatchRepository
                .findPendingDispatchesOrderedByPriority()
                .stream()
                .map(dispatchMapper::toSummaryDTO)
                .toList();
    }

    @Override
    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void processScheduledDispatches() {

        log.info("Checking scheduled dispatches...");

        List<Dispatch> dueDispatches =
                dispatchRepository.findScheduledDispatchesDue(
                        LocalDateTime.now());

        if (dueDispatches.isEmpty()) {
            return;
        }

        dueDispatches.forEach(this::addToQueue);

        log.info(
                "{} scheduled dispatch(es) moved into queue.",
                dueDispatches.size()
        );
    }

    @Override
    @Transactional
    public void retryFailedDispatch(String dispatchId) {

        Dispatch dispatch = dispatchRepository.findById(dispatchId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Dispatch not found."
                        ));

        if (dispatch.getStatus() != DispatchStatus.FAILED) {
            throw new IllegalStateException(
                    "Only FAILED dispatches can be retried."
            );
        }

        if (dispatch.getRetryCount() >= MAX_RETRIES) {
            log.warn(
                    "Dispatch {} exceeded retry limit.",
                    dispatchId
            );
            return;
        }

        dispatch.setRetryCount(
                dispatch.getRetryCount() + 1
        );

        dispatch.setStatus(
                DispatchStatus.PENDING
        );

        dispatchRepository.save(dispatch);

        log.info(
                "Dispatch {} queued for retry.",
                dispatchId
        );
    }
    @Override
    @Transactional
    public void addToQueue(Dispatch dispatch) {

        if (dispatch.getStatus() == DispatchStatus.PENDING) {
            return;
        }

        dispatch.setStatus(
                DispatchStatus.PENDING
        );

        dispatchRepository.save(dispatch);

        log.info(
                "Dispatch {} added to queue.",
                dispatch.getId()
        );
    }
    @Override
    public void removeFromQueue(Dispatch dispatch) {

        log.info(
                "Dispatch {} removed from queue.",
                dispatch.getId()
        );

    }

    @Override
    @Transactional
    public void updatePriority(
            String dispatchId,
            int priority) {

        Dispatch dispatch =
                dispatchRepository.findById(dispatchId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Dispatch not found."
                                ));

        dispatch.setPriority(priority);

        dispatchRepository.save(dispatch);

        log.info(
                "Priority updated for dispatch {} -> {}",
                dispatchId,
                priority
        );
    }
}