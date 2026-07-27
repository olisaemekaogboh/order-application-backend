// mapper/RevenueReportMapper.java
package com.inkfront.logisticsApplication.mapper;

import com.inkfront.logisticsApplication.domain.entity.RevenueReport;
import com.inkfront.logisticsApplication.dto.response.revenue.DailyRevenueDTO;
import com.inkfront.logisticsApplication.dto.response.revenue.RevenueReportDTO;
import com.inkfront.logisticsApplication.dto.response.revenue.WeeklyRevenueDTO;
import com.inkfront.logisticsApplication.dto.response.revenue.MonthlyRevenueDTO;
import com.inkfront.logisticsApplication.dto.response.revenue.YearlyRevenueDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class RevenueReportMapper {

    @Mapping(target = "formattedTotalRevenue", expression = "java(formatCurrency(report.getTotalRevenue(), report.getCurrency()))")
    @Mapping(target = "formattedAverageOrderValue", expression = "java(formatCurrency(report.getAverageOrderValue(), report.getCurrency()))")
    @Mapping(target = "formattedTotalCommission", expression = "java(formatCurrency(report.getTotalCommission(), report.getCurrency()))")
    @Mapping(target = "formattedNetRevenue", expression = "java(formatCurrency(calculateNetRevenue(report), report.getCurrency()))")
    @Mapping(target = "netRevenue", expression = "java(calculateNetRevenue(report))")
    public abstract RevenueReportDTO toDTO(RevenueReport report);

    public DailyRevenueDTO toDailyDTO(Object daily) {
        return new DailyRevenueDTO();
    }
    @Mapping(target = "weekStartDate", expression = "java(getWeekStartDate(weekly))")
    @Mapping(target = "weekEndDate", expression = "java(getWeekEndDate(weekly))")
    @Mapping(target = "weekNumber", expression = "java(getWeekNumber(weekly))")
    @Mapping(target = "year", expression = "java(getYearFromWeekly(weekly))")
    public abstract WeeklyRevenueDTO toWeeklyDTO(Object weekly);

    @Mapping(target = "monthName", expression = "java(getMonthName(monthly))")
    @Mapping(target = "year", expression = "java(getYearFromMonthly(monthly))")
    @Mapping(target = "monthValue", expression = "java(getMonthValue(monthly))")
    public abstract MonthlyRevenueDTO toMonthlyDTO(Object monthly);

    @Mapping(target = "yearValue", expression = "java(getYearValue(yearly))")
    @Mapping(target = "yearOverYearGrowth", expression = "java(calculateYearOverYearGrowth(yearly))")
    @Mapping(target = "quarterOverQuarterGrowth", expression = "java(calculateQuarterOverQuarterGrowth(yearly))")
    public abstract YearlyRevenueDTO toYearlyDTO(Object yearly);

    public abstract RevenueReport toEntity(RevenueReportDTO revenueReportDTO);

    protected String formatCurrency(Double amount, String currency) {
        if (amount == null) {
            return "0.00";
        }
        if ("NGN".equals(currency)) {
            return "₦" + String.format("%,.2f", amount);
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(amount);
    }

    protected Double calculateNetRevenue(RevenueReport report) {
        if (report.getTotalRevenue() == null || report.getTotalCommission() == null) {
            return 0.0;
        }
        return report.getTotalRevenue() - report.getTotalCommission();
    }

    protected String getDayName(LocalDate date) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ofPattern("EEEE"));
    }

    // Renamed methods to avoid ambiguity
    @Named("getWeekStartDate")
    protected LocalDate getWeekStartDate(Object weekly) {
        return LocalDate.now().with(java.time.DayOfWeek.MONDAY);
    }

    @Named("getWeekEndDate")
    protected LocalDate getWeekEndDate(Object weekly) {
        return LocalDate.now().with(java.time.DayOfWeek.SUNDAY);
    }

    protected Integer getWeekNumber(Object weekly) {
        return LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
    }

    @Named("getYearFromWeekly")
    protected Integer getYearFromWeekly(Object weekly) {
        return LocalDate.now().getYear();
    }

    @Named("getYearFromMonthly")
    protected Integer getYearFromMonthly(Object monthly) {
        return LocalDate.now().getYear();
    }

    protected String getMonthName(Object monthly) {
        return LocalDate.now().getMonth().name();
    }

    protected Integer getMonthValue(Object monthly) {
        return LocalDate.now().getMonthValue();
    }

    protected Integer getYearValue(Object yearly) {
        return LocalDate.now().getYear();
    }

    @Named("calculateYearOverYearGrowth")
    protected Double calculateYearOverYearGrowth(Object yearly) {
        return 0.0;
    }

    @Named("calculateQuarterOverQuarterGrowth")
    protected Double calculateQuarterOverQuarterGrowth(Object yearly) {
        return 0.0;
    }
}