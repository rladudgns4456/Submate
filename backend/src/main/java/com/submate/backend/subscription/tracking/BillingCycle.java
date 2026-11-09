package com.submate.backend.subscription.tracking;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public enum BillingCycle {
    MONTHLY,
    YEARLY;

    LocalDate nextOnOrAfter(LocalDate anchor, LocalDate date) {
        if (!date.isAfter(anchor)) {
            return anchor;
        }

        if (this == MONTHLY) {
            long months = ChronoUnit.MONTHS.between(YearMonth.from(anchor), YearMonth.from(date));
            LocalDate candidate = anchor.plusMonths(months);
            return candidate.isBefore(date) ? anchor.plusMonths(months + 1) : candidate;
        }

        int years = date.getYear() - anchor.getYear();
        LocalDate candidate = anchor.plusYears(years);
        return candidate.isBefore(date) ? anchor.plusYears(years + 1) : candidate;
    }
}
