package org.adt.volunteerscase.dto.event.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "name is null")
    private String name;


    @NotNull(message = "Status cannot be null")
    @Pattern(regexp = "^(ONGOING|IN_PROGRESS|COMPLETED)$",
            message = "Status must be one of: ONGOING, IN_PROGRESS, COMPLETED")
    private String status;

    @Size(max = 5000, message = "Description max length is 5000")
    private String description;

    private Integer coverId;

    @Pattern(regexp = "^(\\+[1-9]\\d{1,14}$|^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$)",
            message = "Coordinator contact must be valid email or E.164 phone")
    private String coordinatorContact;

    @NotNull(message = "maxCapacity is blank")
    private Integer maxCapacity;

    @NotNull(message = "data is null")
    private LocalDateTime dateTimestamp;

    private Integer locationId;

    private Set<Integer> tagIds;
}
