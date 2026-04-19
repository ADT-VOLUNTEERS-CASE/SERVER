
package org.adt.volunteerscase.service;

import org.adt.volunteerscase.dto.userEvent.request.UserEventStatusPatchRequest;
import org.adt.volunteerscase.dto.userEvent.response.UserEventResponse;

public interface UserEventService {

    UserEventResponse createApplication(Integer eventId, Integer currentUserId);

    UserEventResponse updateApplicationStatus(
            Integer eventId,
            Integer userId,
            UserEventStatusPatchRequest request,
            Integer currentCoordinatorId
    );
}