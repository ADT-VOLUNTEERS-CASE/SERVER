package org.adt.volunteerscase.dto.userEvent.response;

import lombok.*;
import org.adt.volunteerscase.entity.event.EventStatus;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoordinatorEventApplicationsSummaryResponse {

    private Integer eventId;
    private String eventName;
    private EventStatus eventStatus;
    private LocalDateTime dateTimestamp;
    private Integer maxCapacity;

    private Long applicationsTotal;
    private Long pendingCount;
    private Long acceptedCount;
    private Long rejectedCount;
    private Long revokedCount;
}
