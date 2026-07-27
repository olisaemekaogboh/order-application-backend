package com.inkfront.logisticsApplication.repository;

import com.inkfront.logisticsApplication.domain.entity.RevenueReport;
import com.inkfront.logisticsApplication.domain.enums.ReportPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RevenueReportRepository extends JpaRepository<RevenueReport, String> {

    Optional<RevenueReport> findByReportPeriodAndStartDateAndEndDate(
            ReportPeriod reportPeriod,
            LocalDate startDate,
            LocalDate endDate
    );

    List<RevenueReport> findByReportPeriod(ReportPeriod reportPeriod);

    List<RevenueReport> findByGeneratedBy(String generatedBy);

    List<RevenueReport> findByArchivedTrue();

    @Query("SELECT rr FROM RevenueReport rr WHERE rr.startDate >= :startDate AND rr.endDate <= :endDate")
    List<RevenueReport> findReportsWithinDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT rr FROM RevenueReport rr WHERE rr.reportPeriod = :period AND rr.generatedAt >= :date")
    List<RevenueReport> findRecentReportsByPeriod(
            @Param("period") ReportPeriod period,
            @Param("date") LocalDate date
    );

    @Query("SELECT rr FROM RevenueReport rr ORDER BY rr.generatedAt DESC")
    List<RevenueReport> findLatestReports();
}