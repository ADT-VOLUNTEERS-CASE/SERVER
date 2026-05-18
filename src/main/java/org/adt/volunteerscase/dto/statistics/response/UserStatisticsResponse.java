package org.adt.volunteerscase.dto.statistics.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatisticsResponse {

    private Long totalParticipatedEvents;
    private Long monthlyParticipatedEvents;
    private Long monthlyWorkedMinutes;
    private Long totalWorkedMinutes;
    private Integer currentParticipationStreakMonths;
    private Integer maxParticipationStreakMonths;
    @Builder.Default
    private List<MonthlyParticipationResponse> lastFiveMonthsParticipation = List.of();

}