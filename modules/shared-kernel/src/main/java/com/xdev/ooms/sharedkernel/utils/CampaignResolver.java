package com.xdev.ooms.sharedkernel.utils;

import java.time.LocalDate;
import java.time.MonthDay;

public final class CampaignResolver {

    public static final int DEFAULT_START_MONTH = 9;
    public static final int DEFAULT_START_DAY = 1;
    public static final int DEFAULT_END_MONTH = 4;
    public static final int DEFAULT_END_DAY = 30;

    private CampaignResolver() {
    }

    public static String resolveCampaignLabel(LocalDate referenceDate) {
        return resolveCampaignLabel(
                referenceDate,
                DEFAULT_START_MONTH,
                DEFAULT_START_DAY,
                DEFAULT_END_MONTH,
                DEFAULT_END_DAY
        );
    }

    public static String resolveCampaignLabel(
            LocalDate referenceDate,
            Integer startMonth,
            Integer startDay,
            Integer endMonth,
            Integer endDay
    ) {
        LocalDate effectiveDate = referenceDate == null ? LocalDate.now() : referenceDate;
        MonthDay start = normalizeMonthDay(startMonth, startDay, DEFAULT_START_MONTH, DEFAULT_START_DAY);
        normalizeMonthDay(endMonth, endDay, DEFAULT_END_MONTH, DEFAULT_END_DAY);

        MonthDay today = MonthDay.from(effectiveDate);
        int startYear = today.compareTo(start) >= 0 ? effectiveDate.getYear() : effectiveDate.getYear() - 1;
        return startYear + "/" + (startYear + 1);
    }

    private static MonthDay normalizeMonthDay(Integer month, Integer day, int defaultMonth, int defaultDay) {
        int safeMonth = month == null || month < 1 || month > 12 ? defaultMonth : month;
        int safeDay = day == null || day < 1 || day > 31 ? defaultDay : day;
        safeDay = Math.min(safeDay, MonthDay.of(safeMonth, 1).atYear(2000).getMonth().length(false));
        return MonthDay.of(safeMonth, safeDay);
    }
}
