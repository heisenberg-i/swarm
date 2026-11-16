// PURPOSE: Enumerated value used by SWARM DataQuality domain logic.
package com.biopower.model.enums;

/**
 * Telemetry data-quality for persisted readings.
 * Distinct from Partner {@link ReadingQuality} catalogue codes.
 */
public enum DataQuality {
    MEASURED,
    BAD,
    STALE,
    INCOMPLETE,
    CLOCK_INFERRED,
    DUPLICATE
}
