package pe.edu.upc.predictivemaintain.maintenance.application.queryservices;

import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetAvailabilityReportQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetComparisonReportQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetHistoryExportQuery;
import pe.edu.upc.predictivemaintain.maintenance.domain.model.queries.GetOeeReportQuery;

import java.util.List;

public interface ReportQueryService {

    AvailabilityReport handle(GetAvailabilityReportQuery query);

    List<ComparisonRow> handle(GetComparisonReportQuery query);

    /** Fails with REPORT_EMPTY (404) when there is nothing to export. */
    List<HistoryRow> handle(GetHistoryExportQuery query);

    OeeReport handle(GetOeeReportQuery query);
}