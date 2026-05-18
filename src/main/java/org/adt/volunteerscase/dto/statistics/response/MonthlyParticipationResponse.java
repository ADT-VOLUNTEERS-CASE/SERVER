package org.adt.volunteerscase.dto.statistics.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyParticipationResponse {

    private String monthName;
    private Long participatedEvents;

}
