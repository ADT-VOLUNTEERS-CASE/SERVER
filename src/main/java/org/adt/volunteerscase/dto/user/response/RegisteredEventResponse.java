package org.adt.volunteerscase.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisteredEventResponse {

    private Integer eventId;
    private String name;
    private String eventStatus;
    private String applicationStatus;
    private Integer maxCapacity;
    private Integer weightMinutes;
    private LocalDateTime dateTimestamp;
    private String locationAddress;
    private Integer coordinatorId;
    private String coordinatorFullName;
}
