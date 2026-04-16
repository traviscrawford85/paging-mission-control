package com.traviscrawford.radar.processor;

import com.traviscrawford.radar.model.Alert;
import com.traviscrawford.radar.model.TelemetryRecord;
import com.traviscrawford.radar.parser.TelemetryParser;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelemetryProcessorTest {

    private final TelemetryProcessor processor = new TelemetryProcessor();

    // --- Core rule ---

    @Test
    void shouldFireWhenThreeViolationsOccurWithinWindow() {
        List<TelemetryRecord> records = List.of(
                record("2020-01-01T00:00:00Z", 1000, 101, 20, 105, "TSTAT"),
                record("2020-01-01T00:01:00Z", 1000, 101, 20, 106, "TSTAT"),
                record("2020-01-01T00:02:00Z", 1000, 101, 20, 107, "TSTAT")
        );

        List<Alert> alerts = processor.process(records);

        assertEquals(1, alerts.size());
        assertEquals(Alert.SEVERITY_RED_HIGH, alerts.get(0).getSeverity());
        assertEquals(Instant.parse("2020-01-01T00:00:00Z"), alerts.get(0).getTimestamp());
    }

    @Test
    void shouldNotFireOnTwoViolationsWithinWindow() {
        List<TelemetryRecord> records = List.of(
                record("2020-01-01T00:00:00Z", 1000, 101, 20, 105, "TSTAT"),
                record("2020-01-01T00:01:00Z", 1000, 101, 20, 106, "TSTAT")
        );

        List<Alert> alerts = processor.process(records);

        assertTrue(alerts.isEmpty());
    }

    // --- Time window ---

    @Test
    void shouldFireWhenThreeBreachesOccurExactlyWithinFiveMinutes() {
        List<TelemetryRecord> records = List.of(
                record("2018-01-01T23:00:00.000Z", 1000, 101, 20, 102, "TSTAT"),
                record("2018-01-01T23:02:30.000Z", 1000, 101, 20, 102, "TSTAT"),
                record("2018-01-01T23:05:00.000Z", 1000, 101, 20, 102, "TSTAT")
        );

        List<Alert> alerts = processor.process(records);

        assertEquals(1, alerts.size());
        assertEquals(Instant.parse("2018-01-01T23:00:00Z"), alerts.get(0).getTimestamp());
    }

    @Test
    void shouldNotFireWhenThirdBreachExceedsFiveMinuteWindow() {
        List<TelemetryRecord> records = List.of(
                record("2018-01-01T23:00:00.000Z", 1000, 101, 20, 102, "TSTAT"),
                record("2018-01-01T23:02:30.000Z", 1000, 101, 20, 102, "TSTAT"),
                record("2018-01-01T23:05:00.001Z", 1000, 101, 20, 102, "TSTAT")
        );

        assertTrue(processor.process(records).isEmpty());
    }

    // --- Reset behavior ---

    @Test
    void shouldResetWindowAfterAlertAndRequireThreeFreshBreaches() {
        List<TelemetryRecord> records = List.of(
                record("2018-01-01T23:00:00Z", 1000, 101, 20, 102, "TSTAT"),
                record("2018-01-01T23:00:01Z", 1000, 101, 20, 102, "TSTAT"),
                record("2018-01-01T23:00:02Z", 1000, 101, 20, 102, "TSTAT"),
                record("2018-01-01T23:00:03Z", 1000, 101, 20, 102, "TSTAT"),
                record("2018-01-01T23:00:04Z", 1000, 101, 20, 102, "TSTAT")
        );

        List<Alert> alerts = processor.process(records);

        assertEquals(1, alerts.size());
        assertEquals(Instant.parse("2018-01-01T23:00:00Z"), alerts.get(0).getTimestamp());
    }

    // --- Isolation ---

    @Test
    void shouldIsolateViolationsBetweenSatellites() {
        List<TelemetryRecord> records = List.of(
                record("2020-01-01T00:00:00Z", 1000, 101, 20, 102, "TSTAT"),
                record("2020-01-01T00:00:01Z", 1001, 101, 20, 102, "TSTAT"),
                record("2020-01-01T00:00:02Z", 1000, 101, 20, 102, "TSTAT"),
                record("2020-01-01T00:00:03Z", 1001, 101, 20, 102, "TSTAT")
        );

        assertTrue(processor.process(records).isEmpty());
    }

    @Test
    void shouldIsolateViolationsBetweenComponentsOnSameSatellite() {
        List<TelemetryRecord> records = List.of(
                // TSTAT (2 violations)
                record("2020-01-01T00:00:00Z", 1000, 101, 20, 105, "TSTAT"),
                record("2020-01-01T00:01:00Z", 1000, 101, 20, 106, "TSTAT"),
                // BATT (2 violations)
                record("2020-01-01T00:02:00Z", 1000, 17, 8, 7, "BATT"),
                record("2020-01-01T00:03:00Z", 1000, 17, 8, 6, "BATT")
        );

        List<Alert> alerts = processor.process(records);

        assertTrue(alerts.isEmpty());
    }

    // --- Acceptance: full README sample ---

    @Test
    void shouldProduceDocumentedAlertsForSampleInput() {
        TelemetryParser parser = new TelemetryParser();
        List<TelemetryRecord> records = List.of(
                "20180101 23:01:05.001|1001|101|98|25|20|99.9|TSTAT",
                "20180101 23:01:09.521|1000|17|15|9|8|7.8|BATT",
                "20180101 23:01:26.011|1001|101|98|25|20|99.8|TSTAT",
                "20180101 23:01:38.001|1000|101|98|25|20|102.9|TSTAT",
                "20180101 23:01:49.021|1000|101|98|25|20|87.9|TSTAT",
                "20180101 23:02:09.014|1001|101|98|25|20|89.3|TSTAT",
                "20180101 23:02:10.021|1001|101|98|25|20|89.4|TSTAT",
                "20180101 23:02:11.302|1000|17|15|9|8|7.7|BATT",
                "20180101 23:03:03.008|1000|101|98|25|20|102.7|TSTAT",
                "20180101 23:03:05.009|1000|101|98|25|20|101.2|TSTAT",
                "20180101 23:04:06.017|1001|101|98|25|20|89.9|TSTAT",
                "20180101 23:04:11.531|1000|17|15|9|8|7.9|BATT",
                "20180101 23:05:05.021|1001|101|98|25|20|89.9|TSTAT",
                "20180101 23:05:07.421|1001|17|15|9|8|7.9|BATT"
        ).stream().map(parser::parse).toList();

        List<Alert> alerts = processor.process(records);

        assertEquals(2, alerts.size());

        Alert tstat = alerts.get(0);
        assertEquals(1000, tstat.getSatelliteId());
        assertEquals(Alert.SEVERITY_RED_HIGH, tstat.getSeverity());
        assertEquals("TSTAT", tstat.getComponent());
        assertEquals(Instant.parse("2018-01-01T23:01:38.001Z"), tstat.getTimestamp());

        Alert batt = alerts.get(1);
        assertEquals(1000, batt.getSatelliteId());
        assertEquals(Alert.SEVERITY_RED_LOW, batt.getSeverity());
        assertEquals("BATT", batt.getComponent());
        assertEquals(Instant.parse("2018-01-01T23:01:09.521Z"), batt.getTimestamp());
    }

    // Test helper: constructs a TelemetryRecord directly so tests read as behavior,
    // not as parsing exercises. Field order mirrors the TelemetryRecord constructor.
    private static TelemetryRecord record(
            String isoTimestamp,
            int satelliteId,
            double redHigh,
            double redLow,
            double value,
            String component) {
        return new TelemetryRecord(
                Instant.parse(isoTimestamp),
                satelliteId,
                redHigh,
                redLow,
                value,
                component);
    }
}
