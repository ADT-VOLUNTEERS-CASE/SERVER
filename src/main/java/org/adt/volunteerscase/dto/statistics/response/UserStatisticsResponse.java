package org.adt.volunteerscase.dto.statistics.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

}