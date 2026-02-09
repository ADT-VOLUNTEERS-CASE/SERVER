package org.adt.volunteerscase.dto.event.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import org.adt.volunteerscase.entity.event.EventStatus;

@Data
@Builder
public class EventStatusPatchRequest {

    @NotNull(message = "Status cannot be null")
    @Pattern(regexp = "^(ONGOING|IN_PROGRESS|COMPLETED)$",
            message = "Status must be one of: ONGOING, IN_PROGRESS, COMPLETED")
    private String eventStatus;

}
