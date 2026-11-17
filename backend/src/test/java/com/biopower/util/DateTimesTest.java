// PURPOSE: Analytics query timestamps must parse with or without a timezone.
package com.biopower.util;

import com.biopower.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateTimesTest {

    @Test
    void parsesIsoInstant() {
        LocalDateTime parsed = DateTimes.parseUtc("2026-11-16T10:15:30Z", "start");
        assertEquals(LocalDateTime.of(2026, 11, 16, 10, 15, 30), parsed);
    }

    @Test
    void parsesLocalDateTimeWithoutOffset() {
        LocalDateTime parsed = DateTimes.parseUtc("2026-11-16T10:15:30", "end");
        assertEquals(LocalDateTime.of(2026, 11, 16, 10, 15, 30), parsed);
    }

    @Test
    void rejectsGarbage() {
        assertThrows(BadRequestException.class, () -> DateTimes.parseUtc("not-a-date", "start"));
    }
}
