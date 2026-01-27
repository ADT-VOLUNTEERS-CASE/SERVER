package org.adt.volunteerscase.dto.event.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.adt.volunteerscase.dto.cover.CoverEntityDTO;
import org.adt.volunteerscase.dto.location.LocationEntityDTO;
import org.adt.volunteerscase.dto.tag.TagEntityDTO;
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
    private CoverEntityDTO cover;
    private String coordinatorContact;
    private Integer maxCapacity;
    private LocalDateTime dateTimestamp;
    private LocationEntityDTO location;
    private Set<TagEntityDTO> tags;

}
