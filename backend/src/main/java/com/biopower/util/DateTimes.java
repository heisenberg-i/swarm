// PURPOSE: Parses API date-time query parameters with or without a timezone offset.
package com.biopower.util;

import com.biopower.exception.BadRequestException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public final class DateTimes {

    private DateTimes() {
    }

    public static LocalDateTime parseUtc(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("Missing required parameter: " + field);
        }
        String value = raw.trim();
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid " + field + " timestamp");
        }
    }
}
