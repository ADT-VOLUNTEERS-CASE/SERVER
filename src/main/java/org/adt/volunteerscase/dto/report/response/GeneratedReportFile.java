package org.adt.volunteerscase.dto.report.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeneratedReportFile {
    private String fileName;
    private byte[] content;
}
