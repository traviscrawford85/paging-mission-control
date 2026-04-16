package com.traviscrawford.radar.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single telemetry reading from a satellite.
 *
 * Only red threshold values are retained since alerting rules are defined
 * exclusively in terms of red-high and red-low conditions. Yellow thresholds
 * are intentionally omitted to keep the model aligned with actual alert logic.
 *
 * Parsing and input validation are handled upstream; this class assumes well-formed
 * input and focuses on representing and evaluating telemetry state.
 */
public class TelemetryRecord {

    private final Instant timestamp;
    private final int satelliteId;
    private final double redHighLimit;
    private final double redLowLimit;
    private final double value;
    private final String component;

    // Parser is the trust boundary; values are assumed valid at this stage
    public TelemetryRecord(
            Instant timestamp,
            int satelliteId,
            double redHighLimit,
            double redLowLimit,
            double value,
            String component
    ) {
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.component = Objects.requireNonNull(component, "component must not be null");

        this.satelliteId = satelliteId;
        this.redHighLimit = redHighLimit;
        this.redLowLimit = redLowLimit;
        this.value = value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getSatelliteId() {
        return satelliteId;
    }

    public double getRedHighLimit() {
        return redHighLimit;
    }

    public double getRedLowLimit() {
        return redLowLimit;
    }

    public double getValue() {
        return value;
    }

    public String getComponent() {
        return component;
    }

    /**
     * Determines whether this record violates its configured thresholds.
     *
     * Uses strict inequality to match spec language ("exceed" / "under").
     * Only TSTAT and BATT components are evaluated per requirements; other
     * components are ignored for alerting purposes.
     */
    public boolean isViolation() {
        if ("TSTAT".equalsIgnoreCase(component)) {
            return value > redHighLimit;
        }

        if ("BATT".equalsIgnoreCase(component)) {
            return value < redLowLimit;
        }

        // Non-monitored components do not trigger alerts per spec
        return false;
    }

    @Override
    public String toString() {
        return "TelemetryRecord{" +
                "timestamp=" + timestamp +
                ", satelliteId=" + satelliteId +
                ", component='" + component + '\'' +
                ", value=" + value +
                '}';
    }
}