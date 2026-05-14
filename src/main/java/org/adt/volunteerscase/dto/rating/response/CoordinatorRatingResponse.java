package org.adt.volunteerscase.dto.rating.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoordinatorRatingResponse {
    private String period;
    private Integer ratingPosition;
    private Integer coordinatorId;
    private String firstname;
    private String lastname;
    private String patronymic;
    private String workLocation;
    private Long totalWeightMinutes;
}
