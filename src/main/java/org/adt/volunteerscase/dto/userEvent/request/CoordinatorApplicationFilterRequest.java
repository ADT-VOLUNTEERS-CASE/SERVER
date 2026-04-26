package org.adt.volunteerscase.dto.userEvent.request;

import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoordinatorApplicationFilterRequest {

    @Pattern(
            regexp = "(?i)^(PENDING|ACCEPTED|REJECTED|REVOKED)$",
            message = "application status must be one of (case-insensitive): PENDING, ACCEPTED, REJECTED, REVOKED"
    )
    private String status;
}
