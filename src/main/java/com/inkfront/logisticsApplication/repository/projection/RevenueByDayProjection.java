package com.inkfront.logisticsApplication.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RevenueByDayProjection {

    LocalDate getPeriod();

    BigDecimal getAmount();

}