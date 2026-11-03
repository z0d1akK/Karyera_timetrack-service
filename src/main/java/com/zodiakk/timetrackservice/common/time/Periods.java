package com.zodiakk.timetrackservice.common.time;

import com.zodiakk.timetrackservice.common.exception.ConflictException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class Periods {

    private static final long MAX_RANGE_DAYS = 366;

    private Periods() {
    }

    public static void requireWeek(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null || periodEnd == null
                || periodStart.getDayOfWeek() != DayOfWeek.MONDAY
                || !periodEnd.equals(periodStart.plusDays(6))) {
            throw new ConflictException("Отчётный период должен быть неделей с понедельника по воскресенье");
        }
    }

    public static void requireRange(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null || periodEnd == null || periodEnd.isBefore(periodStart)) {
            throw new ConflictException("Окончание периода не может быть раньше начала");
        }

        if (ChronoUnit.DAYS.between(periodStart, periodEnd) > MAX_RANGE_DAYS) {
            throw new ConflictException("Период не может быть длиннее 366 дней");
        }
    }
}
