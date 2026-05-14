package org.adt.volunteerscase.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RatingAggregateDTO {
    private Integer subjectId;
    private Long minutes;
}
