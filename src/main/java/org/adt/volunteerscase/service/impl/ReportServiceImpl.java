package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.report.CoordinatorReportRowDTO;
import org.adt.volunteerscase.dto.report.UserReportRowDTO;
import org.adt.volunteerscase.dto.report.response.GeneratedReportFile;
import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.entity.report.ReportPeriod;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.CoordinatorNotFoundException;
import org.adt.volunteerscase.exception.UserNotFoundException;
import org.adt.volunteerscase.repository.CoordinatorRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.repository.UserEventRepository;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.ReportService;
import org.openpdf.text.*;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final UserRepository userRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final EventRepository eventRepository;
    private final UserEventRepository userEventRepository;

    @Override
    @Transactional(readOnly = true)
    public GeneratedReportFile assembleCoordinatorReport(Integer coordinatorId, ReportPeriod period) {
        CoordinatorEntity coordinator = coordinatorRepository.findById(coordinatorId)
                .orElseThrow(() -> new CoordinatorNotFoundException(
                        "coordinator with id - " + coordinatorId + " not found"
                ));

        UserEntity coordinatorUser = coordinator.getUser();
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = resolveFrom(coordinatorUser.getCreatedAt(), period, to);

        List<CoordinatorReportRowDTO> rows = eventRepository.findCoordinatorReportRows(
                coordinatorId,
                from,
                to
        );

        String title = "Отчётность координатора " + coordinatorId + ":" + fullName(coordinatorUser)
                + " от " + formatDate(from)
                + " до " + formatDate(to);

        byte[] content = generateCoordinatorPdf(title, rows);

        return new GeneratedReportFile(
                "coordinator-report-" + coordinatorId + "-" + period.getValue() + ".pdf",
                content
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GeneratedReportFile assembleUserReport(Integer userId, ReportPeriod period) {
        UserEntity user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("user with id - " + userId + " not found"));

        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = resolveFrom(user.getCreatedAt(), period, to);

        List<UserReportRowDTO> rows = userEventRepository.findUserReportRows(
                userId,
                from,
                to
        );

        String title = "Отчётность пользователя " + userId + ":" + fullName(user)
                + " от " + formatDate(from)
                + " до " + formatDate(to);

        byte[] content = generateUserPdf(title, rows);

        return new GeneratedReportFile(
                "user-report-" + userId + "-" + period.getValue() + ".pdf",
                content
        );
    }

    private byte[] generateCoordinatorPdf(String title, List<CoordinatorReportRowDTO> rows) {
        return generatePdf(title, new String[]{
                "Дата", "Мероприятие", "Вес в часах", "Количество участников", "Место проведения", "Статус"
        }, new float[]{2.2f, 4f, 1.5f, 2f, 3f, 2f}, rows.stream()
                .map(row -> new String[]{
                        formatDate(row.getEventDate()),
                        row.getEventName(),
                        formatHours(row.getWeightMinutes()),
                        String.valueOf(row.getParticipantsCount()),
                        safe(row.getLocationAddress()),
                        formatEventStatus(row.getEventStatus())
                })
                .toList());
    }

    private byte[] generateUserPdf(String title, List<UserReportRowDTO> rows) {
        return generatePdf(title, new String[]{
                "Дата участия", "Мероприятие", "Вес в часах", "Координатор", "Место проведения",
                "Статус заявки", "Статус участия"
        }, new float[]{2.1f, 3.3f, 1.4f, 3f, 3f, 2.2f, 2.2f}, rows.stream()
                .map(row -> new String[]{
                        formatDate(row.getParticipationDate()),
                        row.getEventName(),
                        formatHours(row.getWeightMinutes()),
                        fullName(row.getCoordinatorLastname(), row.getCoordinatorFirstname(), row.getCoordinatorPatronymic()),
                        safe(row.getLocationAddress()),
                        formatApplicationStatus(row),
                        formatParticipationStatus(row)
                })
                .toList());
    }

    private byte[] generatePdf(String title, String[] headers, float[] widths, List<String[]> rows) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            BaseFont baseFont = loadBaseFont();
            Font titleFont = new Font(baseFont, 14, Font.BOLD);
            Font headerFont = new Font(baseFont, 9, Font.BOLD);
            Font cellFont = new Font(baseFont, 8, Font.NORMAL);

            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(14);
            document.add(titleParagraph);

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);
            table.setWidths(widths);

            for (String header : headers) {
                table.addCell(createCell(header, headerFont, true));
            }

            for (String[] row : rows) {
                for (String value : row) {
                    table.addCell(createCell(value, cellFont, false));
                }
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new IllegalStateException("cannot generate report pdf", ex);
        }
    }

    private PdfPCell createCell(String value, Font font, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(safe(value), font));
        cell.setPadding(5f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        if (header) {
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }

        return cell;
    }

    private BaseFont loadBaseFont() throws IOException, DocumentException {
        ClassPathResource resource = new ClassPathResource("fonts/DejaVuSans.ttf");

        if (!resource.exists()) {
            throw new IllegalStateException("font file fonts/DejaVuSans.ttf not found");
        }

        byte[] fontBytes = resource.getInputStream().readAllBytes();

        return BaseFont.createFont(
                "DejaVuSans.ttf",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                true,
                fontBytes,
                null
        );
    }

    private LocalDateTime resolveFrom(LocalDateTime registeredAt, ReportPeriod period, LocalDateTime now) {
        if (period == ReportPeriod.MONTHLY) {
            return now.minusMonths(1);
        }

        return registeredAt != null ? registeredAt : DEFAULT_START_DATE;
    }

    private String formatDate(LocalDateTime value) {
        if (value == null) {
            return "-";
        }

        return value.format(DATE_FORMATTER);
    }

    private String formatHours(Integer weightMinutes) {
        if (weightMinutes == null) {
            return "0 ч";
        }

        if (weightMinutes % 60 == 0) {
            return (weightMinutes / 60) + " ч";
        }

        return String.format(Locale.US, "%.2f ч", weightMinutes / 60.0);
    }

    private String formatEventStatus(EventStatus status) {
        if (status == null) {
            return "-";
        }

        return switch (status) {
            case COMPLETED -> "Проведено";
            case IN_PROGRESS -> "В процессе";
            case ONGOING -> "Ожидается";
        };
    }

    private String formatApplicationStatus(UserReportRowDTO row) {
        if (row.isRevoked()) {
            return "Отозвана";
        }
        if (row.isRejected()) {
            return "Отклонён";
        }
        if (row.isAccepted()) {
            return "Принят";
        }
        return "Ожидается";
    }

    private String formatParticipationStatus(UserReportRowDTO row) {
        if (row.getDeletedAt() != null) {
            return "Исключён";
        }
        if (row.isRevoked()) {
            return "Отмена";
        }
        if (row.isAccepted() && row.getEventStatus() == EventStatus.COMPLETED) {
            return "Успешно";
        }
        if (row.isRejected()) {
            return "Отмена";
        }
        return "Ожидается";
    }

    private String fullName(UserEntity user) {
        return fullName(user.getLastname(), user.getFirstname(), user.getPatronymic());
    }

    private String fullName(String lastname, String firstname, String patronymic) {
        StringBuilder builder = new StringBuilder();

        if (lastname != null && !lastname.isBlank()) {
            builder.append(lastname);
        }
        if (firstname != null && !firstname.isBlank()) {
            appendWithSpace(builder, firstname);
        }
        if (patronymic != null && !patronymic.isBlank()) {
            appendWithSpace(builder, patronymic);
        }

        return builder.isEmpty() ? "-" : builder.toString();
    }

    private void appendWithSpace(StringBuilder builder, String value) {
        if (!builder.isEmpty()) {
            builder.append(" ");
        }
        builder.append(value);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
