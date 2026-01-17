package org.adt.volunteerscase.dto.event.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.adt.volunteerscase.entity.CoverEntity;
import org.adt.volunteerscase.entity.LocationEntity;
import org.adt.volunteerscase.entity.TagEntity;
import org.adt.volunteerscase.entity.event.EventStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetAllResponse {

    private Integer eventId;
    private EventStatus status;
    private String name;
    private String description;
    private CoverEntity cover;
    private String coordinatorContact;
    private Integer maxCapacity;
    private LocalDateTime dateTimestamp;
    private LocationEntity location;
    private Set<TagEntity> tags;

}
