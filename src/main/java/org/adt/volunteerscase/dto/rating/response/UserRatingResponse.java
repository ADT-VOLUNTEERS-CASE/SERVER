package org.adt.volunteerscase.dto.rating.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRatingResponse {
    private String period;
    private Integer ratingPosition;
    private Integer userId;
    private String firstname;
    private String lastname;
    private String patronymic;
    private Long workedMinutes;
}