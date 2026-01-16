package org.adt.volunteerscase.service;


import org.adt.volunteerscase.dto.event.request.EventCreateRequest;
import org.adt.volunteerscase.dto.event.request.EventPatchRequest;
import org.adt.volunteerscase.dto.event.request.EventStatusPatchRequest;
import org.adt.volunteerscase.dto.event.response.PatchResponse;

public interface EventService {

    void createEvent(EventCreateRequest request);
    PatchResponse patchEvent(Integer eventId, EventPatchRequest request);
    void updateStatus(Integer eventId, EventStatusPatchRequest request);
    void deleteEvent(Integer eventId);


}

