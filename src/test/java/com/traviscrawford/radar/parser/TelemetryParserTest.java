package com.traviscrawford.radar.parser;

import com.traviscrawford.radar.model.TelemetryRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelemetryParserTest {

    private final TelemetryParser parser = new TelemetryParser();

    @Test
    void shouldParseSampleLineAndProduceUtcInstant() {
        TelemetryRecord record = parser.parse("20180101 23:01:38.001|1000|101|98|25|20|102.9|TSTAT");

        assertEquals(Instant.parse("2018-01-01T23:01:38.001Z"), record.getTimestamp());
        assertEquals(1000, record.getSatelliteId());
        assertEquals(101.0, record.getRedHighLimit());
        assertEquals(20.0, record.getRedLowLimit());
        assertEquals(102.9, record.getValue());
        assertEquals("TSTAT", record.getComponent());
    }
}
