package org.adt.volunteerscase.dto.userEvent.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoordinatorApplicationResponse {

    private Integer eventId;
    private String eventName;

    private Integer userId;
    private String firstname;
    private String lastname;
    private String patronymic;
    private String phoneNumber;
    private String email;

    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime revokedAt;
}