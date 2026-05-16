package org.adt.volunteerscase.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.adt.volunteerscase.entity.event.EventStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CoordinatorReportRowDTO {
    private LocalDateTime eventDate;
    private String eventName;
    private Integer weightMinutes;
    private Long participantsCount;
    private String locationAddress;
    private EventStatus eventStatus;
}