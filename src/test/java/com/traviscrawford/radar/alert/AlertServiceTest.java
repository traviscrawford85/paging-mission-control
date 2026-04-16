package com.traviscrawford.radar.alert;

import com.traviscrawford.radar.model.Alert;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertServiceTest {

    private final AlertService alertService = new AlertService();

    @Test
    void serializesSampleAlertsToExpectedJson() {
        List<Alert> alerts = List.of(
                new Alert(1000, Alert.SEVERITY_RED_HIGH, "TSTAT", Instant.parse("2018-01-01T23:01:38.001Z")),
                new Alert(1000, Alert.SEVERITY_RED_LOW, "BATT", Instant.parse("2018-01-01T23:01:09.521Z"))
        );

        String expected = """
                [
                    {
                        "satelliteId": 1000,
                        "severity": "RED HIGH",
                        "component": "TSTAT",
                        "timestamp": "2018-01-01T23:01:38.001Z"
                    },
                    {
                        "satelliteId": 1000,
                        "severity": "RED LOW",
                        "component": "BATT",
                        "timestamp": "2018-01-01T23:01:09.521Z"
                    }
                ]""";

        assertEquals(expected, alertService.toJsonArray(alerts));
    }

    @Test
    void emptyListSerializesAsEmptyArray() {
        assertEquals("[]", alertService.toJsonArray(List.of()));
    }
}
