package org.adt.volunteerscase.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.report.response.GeneratedReportFile;
import org.adt.volunteerscase.entity.report.ReportPeriod;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.adt.volunteerscase.service.ReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v2/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "Reports", description = "API генерации PDF-отчётов")
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "генерация PDF-отчёта текущего координатора",
            description = "Формирует PDF-таблицу мероприятий текущего координатора за monthly или overall период."
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/coordinator/assemble_report_file")
    public ResponseEntity<byte[]> assembleCurrentCoordinatorReport(
            @Parameter(description = "Период отчёта: monthly или overall", example = "monthly")
            @RequestParam(defaultValue = "monthly")
            String period,

            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        GeneratedReportFile report = reportService.assembleCoordinatorReport(
                currentUser.getUser().getUserId(),
                ReportPeriod.fromRequest(period)
        );

        return toPdfResponse(report);
    }

    @Operation(
            summary = "генерация PDF-отчёта координатора администратором",
            description = "Формирует PDF-таблицу мероприятий выбранного координатора за monthly или overall период."
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/admin/assemble_coordinator_report_file")
    public ResponseEntity<byte[]> assembleCoordinatorReportByAdmin(
            @Parameter(description = "ID координатора", example = "2")
            @Positive(message = "coordinator id must be positive")
            @RequestParam("id")
            Integer coordinatorId,

            @Parameter(description = "Период отчёта: monthly или overall", example = "monthly")
            @RequestParam(defaultValue = "monthly")
            String period
    ) {
        GeneratedReportFile report = reportService.assembleCoordinatorReport(
                coordinatorId,
                ReportPeriod.fromRequest(period)
        );

        return toPdfResponse(report);
    }

    @Operation(
            summary = "генерация PDF-отчёта пользователя администратором",
            description = "Формирует PDF-таблицу участий выбранного пользователя за monthly или overall период."
    )
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/admin/assemble_user_report_file")
    public ResponseEntity<byte[]> assembleUserReportByAdmin(
            @Parameter(description = "ID пользователя", example = "3")
            @Positive(message = "user id must be positive")
            @RequestParam("id")
            Integer userId,

            @Parameter(description = "Период отчёта: monthly или overall", example = "monthly")
            @RequestParam(defaultValue = "monthly")
            String period
    ) {
        GeneratedReportFile report = reportService.assembleUserReport(
                userId,
                ReportPeriod.fromRequest(period)
        );

        return toPdfResponse(report);
    }

    private ResponseEntity<byte[]> toPdfResponse(GeneratedReportFile report) {
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(report.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(report.getContent());
    }
}