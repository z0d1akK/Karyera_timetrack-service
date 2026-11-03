package com.zodiakk.timetrackservice.common.time;

import com.zodiakk.timetrackservice.common.exception.ConflictException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PeriodsTest {

    @Test
    void rejectsMissingOrTooLongRanges() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        assertThrows(ConflictException.class, () -> Periods.requireWeek(null, start));
        assertThrows(ConflictException.class, () -> Periods.requireRange(null, start));
        assertThrows(ConflictException.class, () -> Periods.requireRange(start, start.plusDays(400)));
        assertDoesNotThrow(() -> Periods.requireRange(start, start.plusDays(10)));
        assertDoesNotThrow(() -> Periods.requireWeek(LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 25)));
    }
}
