package org.adt.volunteerscase.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.adt.volunteerscase.entity.event.EventStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserReportRowDTO {
    private LocalDateTime participationDate;
    private String eventName;
    private Integer weightMinutes;
    private String coordinatorLastname;
    private String coordinatorFirstname;
    private String coordinatorPatronymic;
    private String locationAddress;
    private boolean accepted;
    private boolean rejected;
    private boolean revoked;
    private LocalDateTime deletedAt;
    private EventStatus eventStatus;
}
