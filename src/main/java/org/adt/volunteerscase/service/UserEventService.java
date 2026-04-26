
package org.adt.volunteerscase.service;

import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.dto.userEvent.request.CoordinatorApplicationFilterRequest;
import org.adt.volunteerscase.dto.userEvent.request.UserEventStatusPatchRequest;
import org.adt.volunteerscase.dto.userEvent.response.CoordinatorApplicationResponse;
import org.adt.volunteerscase.dto.userEvent.response.CoordinatorEventApplicationsSummaryResponse;
import org.adt.volunteerscase.dto.userEvent.response.UserEventResponse;
import org.springframework.data.domain.Pageable;

public interface UserEventService {

    UserEventResponse createApplication(Integer eventId, Integer currentUserId);

    UserEventResponse updateApplicationStatus(
            Integer eventId,
            Integer userId,
            UserEventStatusPatchRequest request,
            Integer currentCoordinatorId
    );

    UserEventResponse getMyApplicationStatus(Integer eventId, Integer currentUserId);

    PageResponse<CoordinatorEventApplicationsSummaryResponse> getMyEventApplicationSummaries(
            Integer currentCoordinatorId,
            Pageable pageable
    );

    PageResponse<CoordinatorApplicationResponse> getApplicationsForMyEvent(
            Integer eventId,
            CoordinatorApplicationFilterRequest filter,
            Integer currentCoordinatorId,
            Pageable pageable
    );
}