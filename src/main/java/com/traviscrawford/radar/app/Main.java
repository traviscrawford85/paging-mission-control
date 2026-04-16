package com.traviscrawford.radar.app;

import com.traviscrawford.radar.alert.AlertService;
import com.traviscrawford.radar.model.Alert;
import com.traviscrawford.radar.model.TelemetryRecord;
import com.traviscrawford.radar.parser.TelemetryParser;
import com.traviscrawford.radar.processor.TelemetryProcessor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Application entry point.
 *
 * Orchestrates parsing, processing, and output of telemetry data.
 * This is the only layer responsible for I/O; domain logic is delegated
 * to parser, processor, and alert components.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: paging-mission-control <telemetry-file>");
            System.exit(1);
        }

        TelemetryParser parser = new TelemetryParser();
        TelemetryProcessor processor = new TelemetryProcessor();
        AlertService alertService = new AlertService();

        List<TelemetryRecord> records = Files.readAllLines(Path.of(args[0])).stream()
                // Skip blank lines to tolerate common file formatting (e.g., trailing newlines)
                .filter(line -> !line.isBlank())
                .map(parser::parse)
                .toList();

        List<Alert> alerts = processor.process(records);
        System.out.println(alertService.toJsonArray(alerts));
    }
}
