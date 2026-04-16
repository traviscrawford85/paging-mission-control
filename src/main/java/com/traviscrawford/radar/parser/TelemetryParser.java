package com.traviscrawford.radar.parser;

import com.traviscrawford.radar.model.TelemetryRecord;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Converts a raw input line (pipe-delimited text) into a TelemetryRecord.
 *
 * Acts as the parsing and validation boundary. Downstream components operate
 * on structured records and do not perform additional parsing.
 *
 * Expected format:
 *   <timestamp>|<satellite-id>|<red-high>|<yellow-high>|<yellow-low>|<red-low>|<raw-value>|<component>
 *
 * Timestamps use the format yyyyMMdd HH:mm:ss.SSS and are interpreted as UTC
 * to ensure deterministic time comparisons.
 */
public class TelemetryParser {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss.SSS");

    private static final String DELIMITER = "\\|";
    private static final int EXPECTED_FIELD_COUNT = 8;

    public TelemetryRecord parse(String line) {
        String[] fields = line.split(DELIMITER);

        // Fail fast on malformed input to avoid silent field misalignment downstream.
        // Although input is assumed well-formed, a mismatch here would corrupt parsing.
        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + EXPECTED_FIELD_COUNT + " fields but got " + fields.length + ": " + line);
        }

        // Yellow thresholds are intentionally ignored; alerting logic is defined only on red limits

        // Input timestamps have no timezone; UTC is assumed for deterministic processing
        Instant timestamp = LocalDateTime.parse(fields[0], TIMESTAMP_FORMAT).toInstant(ZoneOffset.UTC);
        int satelliteId = Integer.parseInt(fields[1]);
        double redHigh = Double.parseDouble(fields[2]);
        double redLow = Double.parseDouble(fields[5]);
        double rawValue = Double.parseDouble(fields[6]);
        String component = fields[7];

        return new TelemetryRecord(timestamp, satelliteId, redHigh, redLow, rawValue, component);
    }
}
