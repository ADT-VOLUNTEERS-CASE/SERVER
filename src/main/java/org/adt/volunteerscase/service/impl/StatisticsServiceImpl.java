package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.statistics.response.CoordinatorStatisticsResponse;
import org.adt.volunteerscase.dto.statistics.response.MonthlyParticipationResponse;
import org.adt.volunteerscase.dto.statistics.response.UserStatisticsResponse;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.exception.CoordinatorNotFoundException;
import org.adt.volunteerscase.exception.UserNotFoundException;
import org.adt.volunteerscase.repository.CoordinatorRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.repository.UserEventRepository;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.StatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final int RECENT_PARTICIPATION_MONTHS = 5;
    private static final Locale RU_LOCALE = Locale.forLanguageTag("ru");

    private final UserRepository userRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final UserEventRepository userEventRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics(Integer userId) {
        userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("user with id - " + userId + " not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.minusMonths(1);

        long totalEvents = userEventRepository.countCompletedParticipationsByUserId(userId);
        long monthlyEvents = userEventRepository.countCompletedParticipationsByUserIdBetween(
                userId,
                EventStatus.COMPLETED,
                monthStart,
                now
        );

        long totalMinutes = safe(userEventRepository.sumCompletedParticipationWeightMinutesByUserId(
                userId,
                EventStatus.COMPLETED
        ));
        long monthlyMinutes = safe(userEventRepository.sumCompletedParticipationWeightMinutesByUserIdBetween(
                userId,
                EventStatus.COMPLETED,
                monthStart,
                now
        ));

        List<LocalDateTime> participationDates = userEventRepository.findCompletedParticipationDatesByUserId(
                userId,
                EventStatus.COMPLETED
        );
        Streaks streaks = calculateStreaks(participationDates, YearMonth.from(now));
        List<MonthlyParticipationResponse> lastFiveMonthsParticipation = calculateLastFiveMonthsParticipation(
                participationDates,
                YearMonth.from(now)
        );

        return UserStatisticsResponse.builder()
                .totalParticipatedEvents(totalEvents)
                .monthlyParticipatedEvents(monthlyEvents)
                .monthlyWorkedMinutes(monthlyMinutes)
                .totalWorkedMinutes(totalMinutes)
                .currentParticipationStreakMonths(streaks.current())
                .maxParticipationStreakMonths(streaks.max())
                .lastFiveMonthsParticipation(lastFiveMonthsParticipation)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CoordinatorStatisticsResponse getCoordinatorStatistics(Integer coordinatorId) {
        coordinatorRepository.findById(coordinatorId)
                .orElseThrow(() -> new CoordinatorNotFoundException(
                        "coordinator with id - " + coordinatorId + " not found"
                ));

        return CoordinatorStatisticsResponse.builder()
                .totalCompletedEvents(eventRepository.countCompletedEventsByCoordinatorId(
                        coordinatorId
                ))
                .totalParticipants(userEventRepository.countCompletedEventParticipantsByCoordinatorId(
                        coordinatorId,
                        EventStatus.COMPLETED
                ))
                .totalWeightMinutes(safe(eventRepository.sumCompletedEventWeightMinutesByCoordinatorId(
                        coordinatorId
                )))
                .build();
    }

    private Streaks calculateStreaks(List<LocalDateTime> participationDates, YearMonth currentMonth) {
        if (participationDates == null || participationDates.isEmpty()) {
            return new Streaks(0, 0);
        }

        TreeSet<YearMonth> months = participationDates.stream()
                .map(YearMonth::from)
                .collect(Collectors.toCollection(TreeSet::new));

        int current = 0;
        YearMonth cursor = currentMonth;
        while (months.contains(cursor)) {
            current++;
            cursor = cursor.minusMonths(1);
        }

        int max = 0;
        int running = 0;
        YearMonth previous = null;

        for (YearMonth month : months) {
            if (previous != null && month.equals(previous.plusMonths(1))) {
                running++;
            } else {
                running = 1;
            }

            max = Math.max(max, running);
            previous = month;
        }

        return new Streaks(current, max);
    }

    private List<MonthlyParticipationResponse> calculateLastFiveMonthsParticipation(
            List<LocalDateTime> participationDates,
            YearMonth currentMonth
    ) {
        Map<YearMonth, Long> participationCountByMonth = participationDates.stream()
                .collect(Collectors.groupingBy(YearMonth::from, Collectors.counting()));

        return IntStream.range(0, RECENT_PARTICIPATION_MONTHS)
                .mapToObj(index -> currentMonth.minusMonths(RECENT_PARTICIPATION_MONTHS - 1L - index))
                .map(month -> MonthlyParticipationResponse.builder()
                        .monthName(month.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, RU_LOCALE))
                        .participatedEvents(participationCountByMonth.getOrDefault(month, 0L))
                        .build())
                .toList();
    }

    private long safe(Long value) {
        return value == null ? 0L : value;
    }

    private record Streaks(int current, int max) {
    }
}
