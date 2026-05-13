package org.adt.volunteerscase.dto.statistics.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoordinatorStatisticsResponse {

    private Long totalCompletedEvents;
    private Long totalParticipants;
    private Long totalWeightMinutes;

}