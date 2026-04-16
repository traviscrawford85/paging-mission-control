package com.traviscrawford.radar.processor;

import com.traviscrawford.radar.model.Alert;
import com.traviscrawford.radar.model.TelemetryRecord;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects limit-violation patterns across a stream of telemetry records.
 *
 * Rule:
 *   Three violations for the same satellite and component within a five-minute
 *   window trigger one alert. The alert carries the timestamp of the first
 *   violation in that window. After firing, the window state is reset so the
 *   next alert for the same (satellite, component) pair requires three fresh
 *   violations.
 */
public class TelemetryProcessor {

    private static final Duration WINDOW = Duration.ofMinutes(5);
    private static final int VIOLATION_THRESHOLD = 3;

    public List<Alert> process(List<TelemetryRecord> records) {
        Map<ViolationKey, Deque<TelemetryRecord>> windows = new HashMap<>();
        List<Alert> alerts = new ArrayList<>();

        for (TelemetryRecord record : records) {
            if (!record.isViolation()) {
                continue;
            }

            ViolationKey key = new ViolationKey(record.getSatelliteId(), record.getComponent());
            Deque<TelemetryRecord> window = windows.computeIfAbsent(key, k -> new ArrayDeque<>());

            evictStale(window, record.getTimestamp());
            window.addLast(record);

            if (window.size() >= VIOLATION_THRESHOLD) {
                TelemetryRecord first = window.peekFirst();
                alerts.add(new Alert(
                        first.getSatelliteId(),
                        severityFor(first.getComponent()),
                        first.getComponent(),
                        first.getTimestamp()));
                // Reset window to enforce non-overlapping violation sequences
                window.clear();
            }
        }

        return alerts;
    }

    private static void evictStale(Deque<TelemetryRecord> window, Instant now) {
        // Strict '>' keeps records exactly 5 minutes apart within the window
        while (!window.isEmpty() && Duration.between(window.peekFirst().getTimestamp(), now).compareTo(WINDOW) > 0) {
            window.pollFirst();
        }
    }

    private static String severityFor(String component) {
        if ("TSTAT".equalsIgnoreCase(component)) {
            return Alert.SEVERITY_RED_HIGH;
        }
        if ("BATT".equalsIgnoreCase(component)) {
            return Alert.SEVERITY_RED_LOW;
        }
        // Should not occur: isViolation() filters to known components.
        // Acts as a guard if new components are introduced without updating logic.
        throw new IllegalStateException("Unexpected component: " + component);
    }

    private record ViolationKey(int satelliteId, String component) {
    }
}
