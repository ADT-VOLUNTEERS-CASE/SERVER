package org.adt.volunteerscase.dto.event.response;

import lombok.Builder;
import lombok.Data;
import org.adt.volunteerscase.entity.event.EventStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class PatchResponse {

    private Integer eventId;
    private EventStatus status;
    private String name;
    private String description;
    private Integer coverId;
    private String coordinatorContact;
    private Integer maxCapacity;
    private LocalDateTime dateTimestamp;
    private Integer locationId;
    private String locationAddress;
    private Set<Integer> tagIds;

}
