package org.adt.volunteerscase.dto.userEvent.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEventStatusPatchRequest {

    @NotBlank(message = "application status is blank")
    @Pattern(
            regexp = "^(ACCEPTED|REJECTED)$",
            message = "application status must be one of: ACCEPTED, REJECTED"
    )
    private String status;

    @Size(max = 1000, message = "reject reason max length is 1000")
    @Pattern(regexp = ".*\\S.*", message = "reject reason must not be blank")
    private String rejectReason;
}