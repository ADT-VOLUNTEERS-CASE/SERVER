package org.adt.volunteerscase.service;

import org.adt.volunteerscase.dto.report.response.GeneratedReportFile;
import org.adt.volunteerscase.entity.report.ReportPeriod;

public interface ReportService {
    GeneratedReportFile assembleCoordinatorReport(Integer coordinatorId, ReportPeriod period);

    GeneratedReportFile assembleUserReport(Integer userId, ReportPeriod period);
}
