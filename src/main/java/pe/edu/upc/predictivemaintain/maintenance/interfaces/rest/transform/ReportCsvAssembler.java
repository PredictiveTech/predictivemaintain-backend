package pe.edu.upc.predictivemaintain.maintenance.interfaces.rest.transform;

import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.AssetEvent;
import pe.edu.upc.predictivemaintain.maintenance.application.queryservices.HistoryRow;

import java.time.Instant;
import java.util.List;

/**
 * Builds the CSV of the exported history.
 */
public final class ReportCsvAssembler {

    private static final String HEADER =
            "asset_code,asset_name,event_type,occurred_at,ended_at,severity,status,reference_id";

    private ReportCsvAssembler() {
    }

    public static String toCsv(List<HistoryRow> rows) {
        StringBuilder csv = new StringBuilder(HEADER).append("\r\n");
        for (HistoryRow row : rows) {
            AssetEvent event = row.event();
            csv.append(cell(row.assetCode())).append(',')
                    .append(cell(row.assetName())).append(',')
                    .append(cell(event.type().name())).append(',')
                    .append(cell(text(event.occurredAt()))).append(',')
                    .append(cell(text(event.endedAt()))).append(',')
                    .append(cell(event.severity())).append(',')
                    .append(cell(event.status())).append(',')
                    .append(cell(event.referenceId().toString()))
                    .append("\r\n");
        }
        return csv.toString();
    }

    private static String text(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    /**
     * Quotes the value when needed and neutralizes spreadsheet formulas: a cell that starts with = + - or @
     * would be executed by Excel, so a single quote is put in front of it (CSV injection).
     */
    private static String cell(String value) {
        if (value == null) {
            return "";
        }
        String safe = value;
        if (!safe.isEmpty() && "=+-@\t\r".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            safe = "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }
}