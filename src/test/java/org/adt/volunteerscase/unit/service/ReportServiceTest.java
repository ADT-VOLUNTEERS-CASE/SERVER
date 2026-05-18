package org.adt.volunteerscase.unit.service;

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
import org.adt.volunteerscase.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoordinatorRepository coordinatorRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserEventRepository userEventRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(
                userRepository,
                coordinatorRepository,
                eventRepository,
                userEventRepository
        );
    }

    @Test
    void assembleCoordinatorReport_shouldGenerateMonthlyPdf() {
        UserEntity coordinatorUser = buildUser(2, "Иванов", "Иван", "Иванович",
                LocalDateTime.of(2026, 1, 1, 10, 0));
        CoordinatorEntity coordinator = CoordinatorEntity.builder()
                .userId(2)
                .user(coordinatorUser)
                .workLocation("Main office")
                .build();

        when(coordinatorRepository.findById(2)).thenReturn(Optional.of(coordinator));
        when(eventRepository.findCoordinatorReportRows(eq(2), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        new CoordinatorReportRowDTO(
                                LocalDateTime.of(2026, 5, 10, 12, 0),
                                "Помощь на мероприятии",
                                120,
                                5L,
                                "Москва",
                                EventStatus.COMPLETED
                        )
                ));

        LocalDateTime beforeCall = LocalDateTime.now();

        GeneratedReportFile report = reportService.assembleCoordinatorReport(2, ReportPeriod.MONTHLY);

        LocalDateTime afterCall = LocalDateTime.now();

        assertThat(report.getFileName()).isEqualTo("coordinator-report-2-monthly.pdf");
        assertPdf(report.getContent());

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(eventRepository).findCoordinatorReportRows(eq(2), fromCaptor.capture(), toCaptor.capture());

        assertThat(fromCaptor.getValue())
                .isBetween(beforeCall.minusMonths(1).minusSeconds(1), afterCall.minusMonths(1).plusSeconds(1));
        assertThat(toCaptor.getValue())
                .isBetween(beforeCall.minusSeconds(1), afterCall.plusSeconds(1));

        verify(coordinatorRepository).findById(2);
        verifyNoInteractions(userRepository, userEventRepository);
    }

    @Test
    void assembleCoordinatorReport_shouldUseRegistrationDateForOverallPeriod() {
        LocalDateTime registeredAt = LocalDateTime.of(2025, 11, 16, 10, 0);

        UserEntity coordinatorUser = buildUser(2, "Иванов", "Иван", "Иванович", registeredAt);
        CoordinatorEntity coordinator = CoordinatorEntity.builder()
                .userId(2)
                .user(coordinatorUser)
                .build();

        when(coordinatorRepository.findById(2)).thenReturn(Optional.of(coordinator));
        when(eventRepository.findCoordinatorReportRows(eq(2), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        GeneratedReportFile report = reportService.assembleCoordinatorReport(2, ReportPeriod.OVERALL);

        assertThat(report.getFileName()).isEqualTo("coordinator-report-2-overall.pdf");
        assertPdf(report.getContent());

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(eventRepository).findCoordinatorReportRows(eq(2), fromCaptor.capture(), any(LocalDateTime.class));

        assertThat(fromCaptor.getValue()).isEqualTo(registeredAt);
    }

    @Test
    void assembleCoordinatorReport_shouldThrowException_whenCoordinatorNotFound() {
        when(coordinatorRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.assembleCoordinatorReport(99, ReportPeriod.MONTHLY))
                .isInstanceOf(CoordinatorNotFoundException.class)
                .hasMessage("coordinator with id - 99 not found");

        verify(coordinatorRepository).findById(99);
        verifyNoInteractions(eventRepository, userRepository, userEventRepository);
    }

    @Test
    void assembleUserReport_shouldGenerateMonthlyPdf() {
        UserEntity user = buildUser(3, "Петров", "Пётр", "Петрович",
                LocalDateTime.of(2026, 1, 1, 10, 0));

        when(userRepository.findByUserIdAndDeletedAtIsNull(3)).thenReturn(Optional.of(user));
        when(userEventRepository.findUserReportRows(eq(3), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        new UserReportRowDTO(
                                LocalDateTime.of(2026, 5, 12, 14, 0),
                                "Городское мероприятие",
                                90,
                                "Иванов",
                                "Иван",
                                "Иванович",
                                "Москва",
                                true,
                                false,
                                false,
                                null,
                                EventStatus.COMPLETED
                        )
                ));

        GeneratedReportFile report = reportService.assembleUserReport(3, ReportPeriod.MONTHLY);

        assertThat(report.getFileName()).isEqualTo("user-report-3-monthly.pdf");
        assertPdf(report.getContent());

        verify(userRepository).findByUserIdAndDeletedAtIsNull(3);
        verify(userEventRepository).findUserReportRows(eq(3), any(LocalDateTime.class), any(LocalDateTime.class));
        verifyNoInteractions(coordinatorRepository, eventRepository);
    }

    @Test
    void assembleUserReport_shouldUseRegistrationDateForOverallPeriod() {
        LocalDateTime registeredAt = LocalDateTime.of(2025, 11, 16, 10, 0);
        UserEntity user = buildUser(3, "Петров", "Пётр", "Петрович", registeredAt);

        when(userRepository.findByUserIdAndDeletedAtIsNull(3)).thenReturn(Optional.of(user));
        when(userEventRepository.findUserReportRows(eq(3), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        GeneratedReportFile report = reportService.assembleUserReport(3, ReportPeriod.OVERALL);

        assertThat(report.getFileName()).isEqualTo("user-report-3-overall.pdf");
        assertPdf(report.getContent());

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userEventRepository).findUserReportRows(eq(3), fromCaptor.capture(), any(LocalDateTime.class));

        assertThat(fromCaptor.getValue()).isEqualTo(registeredAt);
    }

    @Test
    void assembleUserReport_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.assembleUserReport(99, ReportPeriod.OVERALL))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("user with id - 99 not found");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(99);
        verifyNoInteractions(coordinatorRepository, eventRepository, userEventRepository);
    }

    private UserEntity buildUser(
            Integer userId,
            String lastname,
            String firstname,
            String patronymic,
            LocalDateTime createdAt
    ) {
        return UserEntity.builder()
                .userId(userId)
                .lastname(lastname)
                .firstname(firstname)
                .patronymic(patronymic)
                .email("user" + userId + "@example.com")
                .phoneNumber("+7999000000" + userId)
                .createdAt(createdAt)
                .build();
    }

    private void assertPdf(byte[] content) {
        assertThat(content).isNotNull();
        assertThat(content.length).isGreaterThan(1000);

        String header = new String(content, 0, 4, StandardCharsets.ISO_8859_1);
        assertThat(header).isEqualTo("%PDF");
    }
}