package org.adt.volunteerscase.dto.event.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.adt.volunteerscase.entity.event.EventStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventPatchRequest {

    @Size(max = 255, message = "Name max length is 255")
    private String name;

    @Size(max = 5000, message = "Description max length is 5000")
    private String description;

    @Pattern(regexp = "^(ONGOING|IN_PROGRESS|COMPLETED)$",
            message = "Status must be one of: ONGOING, IN_PROGRESS, COMPLETED")
    private String eventStatus;

    @Pattern(regexp = "^(\\+[1-9]\\d{1,14}$|^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$)",
            message = "Coordinator contact must be valid email or E.164 phone")
    private String coordinatorContact;

    @Min(value = 1, message = "Max capacity must be greater than 0")
    private Integer maxCapacity;

    private LocalDateTime dateTimestamp;

    @Positive(message = "Location ID must be positive")
    private Integer locationId;

    @Positive(message = "Cover ID must be positive")
    private Integer coverId;

    private Set<@Positive(message = "tag id must be positive") Integer> tagIds;

    private Boolean clearTags = false;


}
