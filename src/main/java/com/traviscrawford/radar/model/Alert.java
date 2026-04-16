package com.traviscrawford.radar.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single alert emitted from a limit-violation pattern.
 *
 * This class reflects the output contract defined by the expected JSON format.
 * Field structure and naming are aligned with that contract and should remain stable.
 *
 * The timestamp corresponds to the first violation in the triggering window,
 * providing context for when the anomaly began.
 */
public class Alert {

    // Severity values match the expected output format directly.
    // An enum is unnecessary here since no additional behavior or mapping is required.
    public static final String SEVERITY_RED_HIGH = "RED HIGH";
    public static final String SEVERITY_RED_LOW = "RED LOW";

    private final int satelliteId;
    private final String severity;
    private final String component;
    private final Instant timestamp;

    // Instances are constructed from validated telemetry processing; no additional validation is performed here.
    public Alert(int satelliteId, String severity, String component, Instant timestamp) {
        this.severity = Objects.requireNonNull(severity, "severity must not be null");
        this.component = Objects.requireNonNull(component, "component must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.satelliteId = satelliteId;
    }

    public int getSatelliteId() {
        return satelliteId;
    }

    public String getSeverity() {
        return severity;
    }

    public String getComponent() {
        return component;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "satelliteId=" + satelliteId +
                ", severity='" + severity + '\'' +
                ", component='" + component + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
