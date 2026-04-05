package org.adt.volunteerscase.dto.event.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.adt.volunteerscase.entity.LocationEntity;
import org.adt.volunteerscase.entity.event.EventStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventCreateRequest {

    @Schema(description = "name for event")
    @NotBlank(message = "name is null")
    private String name;

    @Schema(description = "status for event")
    @NotNull(message = "Status cannot be null")
    @Pattern(regexp = "^(ONGOING|IN_PROGRESS|COMPLETED)$",
            message = "Status must be one of: ONGOING, IN_PROGRESS, COMPLETED")
    private String status;

    @Schema(description = "description")
    @Size(max = 5000, message = "Description max length is 5000")
    private String description;

    private Integer coverId;

    @Positive(message = "coordinator id must be positive")
    @NotNull(message = "coordinator id is null")
    private Integer coordinatorId;

    @Min(value = 1, message = "Max capacity must be greater than 0")
    @NotNull(message = "maxCapacity is blank")
    private Integer maxCapacity;

    @NotNull(message = "data is null")
    private LocalDateTime dateTimestamp;

    @NotNull(message = "location id is null")
    private Integer locationId;

    private Set<Integer> tagIds;
}
