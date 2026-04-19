package org.adt.volunteerscase.dto.userEvent.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEventResponse {

    private Integer userId;
    private Integer eventId;
    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime revokedAt;
}