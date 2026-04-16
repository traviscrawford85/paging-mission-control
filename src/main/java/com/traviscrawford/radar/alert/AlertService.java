package com.traviscrawford.radar.alert;

import com.traviscrawford.radar.model.Alert;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Produces the JSON output contract for alerts.
 *
 * Serializes Alert instances into the expected array format defined by the
 * application output specification.
 */
public class AlertService {

    // Explicit pattern ensures fixed millisecond precision for deterministic output
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private static final String INDENT = "    ";

    public String toJsonArray(List<Alert> alerts) {
        if (alerts.isEmpty()) {
            return "[]";
        }

        StringBuilder out = new StringBuilder();
        out.append("[\n");
        for (int i = 0; i < alerts.size(); i++) {
            appendAlert(out, alerts.get(i));
            if (i < alerts.size() - 1) {
                out.append(",");
            }
            out.append("\n");
        }
        out.append("]");
        return out.toString();
    }

    private void appendAlert(StringBuilder out, Alert alert) {
        out.append(INDENT).append("{\n");
        appendField(out, "satelliteId", Integer.toString(alert.getSatelliteId()), false);
        out.append(",\n");
        appendField(out, "severity", alert.getSeverity(), true);
        out.append(",\n");
        appendField(out, "component", alert.getComponent(), true);
        out.append(",\n");
        appendField(out, "timestamp", TIMESTAMP_FORMAT.format(alert.getTimestamp()), true);
        out.append("\n").append(INDENT).append("}");
    }

    // Field values come from a controlled set (severity, component),
    // so no JSON escaping is required for this use case
    private void appendField(StringBuilder out, String key, String value, boolean quoted) {
        out.append(INDENT).append(INDENT);
        out.append("\"").append(key).append("\": ");
        if (quoted) {
            out.append("\"").append(value).append("\"");
        } else {
            out.append(value);
        }
    }
}
