package com.inkfront.logisticsApplication.repository.dispatch;

import com.inkfront.logisticsApplication.domain.entity.dispatch.DispatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchHistoryRepository extends JpaRepository<DispatchHistory, String> {

    List<DispatchHistory> findByDispatchIdOrderByChangedAtAsc(String dispatchId);
}