package org.adt.volunteerscase.unit.service;

import org.adt.volunteerscase.dto.statistics.response.CoordinatorStatisticsResponse;
import org.adt.volunteerscase.dto.statistics.response.MonthlyParticipationResponse;
import org.adt.volunteerscase.dto.statistics.response.UserStatisticsResponse;
import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.CoordinatorNotFoundException;
import org.adt.volunteerscase.exception.UserNotFoundException;
import org.adt.volunteerscase.repository.CoordinatorRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.repository.UserEventRepository;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.StatisticsService;
import org.adt.volunteerscase.service.impl.StatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoordinatorRepository coordinatorRepository;

    @Mock
    private UserEventRepository userEventRepository;

    @Mock
    private EventRepository eventRepository;

    private StatisticsService statisticsService;
    private UserEntity user;
    private CoordinatorEntity coordinator;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsServiceImpl(
                userRepository,
                coordinatorRepository,
                userEventRepository,
                eventRepository
        );

        user = UserEntity.builder()
                .userId(1)
                .firstname("Иван")
                .lastname("Пользователь")
                .email("user@example.com")
                .phoneNumber("+79990000001")
                .build();

        coordinator = CoordinatorEntity.builder()
                .userId(2)
                .user(UserEntity.builder()
                        .userId(2)
                        .firstname("Анна")
                        .lastname("Координатор")
                        .email("coordinator@example.com")
                        .phoneNumber("+79990000002")
                        .isCoordinator(true)
                        .build())
                .workLocation("Main office")
                .build();
    }

    @Test
    void getUserStatistics_shouldReturnAggregatedValuesAndStreaks() {
        YearMonth currentMonth = YearMonth.now();
        List<LocalDateTime> participationDates = List.of(
                currentMonth.minusMonths(3).atDay(1).atStartOfDay(),
                currentMonth.minusMonths(2).atDay(1).atStartOfDay(),
                currentMonth.minusMonths(2).atDay(2).atStartOfDay(),
                currentMonth.minusMonths(1).atDay(1).atStartOfDay(),
                currentMonth.atDay(1).atStartOfDay()
        );

        when(userRepository.findByUserIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(user));
        when(userEventRepository.countCompletedParticipationsByUserId(1))
                .thenReturn(5L);
        when(userEventRepository.countCompletedParticipationsByUserIdBetween(
                eq(1),
                eq(EventStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(2L);
        when(userEventRepository.sumCompletedParticipationWeightMinutesByUserId(1, EventStatus.COMPLETED))
                .thenReturn(600L);
        when(userEventRepository.sumCompletedParticipationWeightMinutesByUserIdBetween(
                eq(1),
                eq(EventStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(240L);
        when(userEventRepository.findCompletedParticipationDatesByUserId(1, EventStatus.COMPLETED))
                .thenReturn(participationDates);

        UserStatisticsResponse response = statisticsService.getUserStatistics(1);

        assertThat(response.getTotalParticipatedEvents()).isEqualTo(5L);
        assertThat(response.getMonthlyParticipatedEvents()).isEqualTo(2L);
        assertThat(response.getTotalWorkedMinutes()).isEqualTo(600L);
        assertThat(response.getMonthlyWorkedMinutes()).isEqualTo(240L);
        assertThat(response.getCurrentParticipationStreakMonths()).isEqualTo(4);
        assertThat(response.getMaxParticipationStreakMonths()).isEqualTo(4);
        assertThat(response.getLastFiveMonthsParticipation())
                .extracting(
                        MonthlyParticipationResponse::getMonthName,
                        MonthlyParticipationResponse::getParticipatedEvents
                )
                .containsExactly(
                        tuple(monthName(currentMonth.minusMonths(4)), 0L),
                        tuple(monthName(currentMonth.minusMonths(3)), 1L),
                        tuple(monthName(currentMonth.minusMonths(2)), 2L),
                        tuple(monthName(currentMonth.minusMonths(1)), 1L),
                        tuple(monthName(currentMonth), 1L)
                );

        verify(userRepository).findByUserIdAndDeletedAtIsNull(1);
        verify(userEventRepository).countCompletedParticipationsByUserId(1);
        verify(userEventRepository).sumCompletedParticipationWeightMinutesByUserId(1, EventStatus.COMPLETED);
        verify(userEventRepository).findCompletedParticipationDatesByUserId(1, EventStatus.COMPLETED);
    }

    @Test
    void getUserStatistics_shouldReturnZeroStreaks_whenUserHasNoParticipations() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(user));
        when(userEventRepository.countCompletedParticipationsByUserId(1))
                .thenReturn(0L);
        when(userEventRepository.countCompletedParticipationsByUserIdBetween(
                eq(1),
                eq(EventStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(0L);
        when(userEventRepository.sumCompletedParticipationWeightMinutesByUserId(1, EventStatus.COMPLETED))
                .thenReturn(0L);
        when(userEventRepository.sumCompletedParticipationWeightMinutesByUserIdBetween(
                eq(1),
                eq(EventStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(0L);
        when(userEventRepository.findCompletedParticipationDatesByUserId(1, EventStatus.COMPLETED))
                .thenReturn(List.of());

        UserStatisticsResponse response = statisticsService.getUserStatistics(1);

        assertThat(response.getTotalParticipatedEvents()).isZero();
        assertThat(response.getMonthlyParticipatedEvents()).isZero();
        assertThat(response.getTotalWorkedMinutes()).isZero();
        assertThat(response.getMonthlyWorkedMinutes()).isZero();
        assertThat(response.getCurrentParticipationStreakMonths()).isZero();
        assertThat(response.getMaxParticipationStreakMonths()).isZero();
        assertThat(response.getLastFiveMonthsParticipation())
                .hasSize(5)
                .allSatisfy(month -> assertThat(month.getParticipatedEvents()).isZero());
    }

    @Test
    void getUserStatistics_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statisticsService.getUserStatistics(99))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("user with id - 99 not found");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(99);
        verifyNoInteractions(userEventRepository, eventRepository, coordinatorRepository);
    }

    @Test
    void getCoordinatorStatistics_shouldReturnAggregatedValues() {
        when(coordinatorRepository.findById(2)).thenReturn(Optional.of(coordinator));
        when(eventRepository.countCompletedEventsByCoordinatorId(2))
                .thenReturn(4L);
        when(userEventRepository.countCompletedEventParticipantsByCoordinatorId(2, EventStatus.COMPLETED))
                .thenReturn(12L);
        when(eventRepository.sumCompletedEventWeightMinutesByCoordinatorId(2))
                .thenReturn(900L);

        CoordinatorStatisticsResponse response = statisticsService.getCoordinatorStatistics(2);

        assertThat(response.getTotalCompletedEvents()).isEqualTo(4L);
        assertThat(response.getTotalParticipants()).isEqualTo(12L);
        assertThat(response.getTotalWeightMinutes()).isEqualTo(900L);

        verify(coordinatorRepository).findById(2);
        verify(eventRepository).countCompletedEventsByCoordinatorId(2);
        verify(userEventRepository).countCompletedEventParticipantsByCoordinatorId(2, EventStatus.COMPLETED);
        verify(eventRepository).sumCompletedEventWeightMinutesByCoordinatorId(2);
    }

    @Test
    void getCoordinatorStatistics_shouldThrowException_whenCoordinatorNotFound() {
        when(coordinatorRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statisticsService.getCoordinatorStatistics(99))
                .isInstanceOf(CoordinatorNotFoundException.class)
                .hasMessage("coordinator with id - 99 not found");

        verify(coordinatorRepository).findById(99);
        verifyNoInteractions(userEventRepository, eventRepository, userRepository);
    }

    private String monthName(YearMonth month) {
        return month.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"));
    }
}
