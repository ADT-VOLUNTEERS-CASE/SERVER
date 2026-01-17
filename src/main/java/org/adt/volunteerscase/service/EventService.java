package org.adt.volunteerscase.service;


import org.adt.volunteerscase.dto.event.request.EventCreateRequest;
import org.adt.volunteerscase.dto.event.request.EventPatchRequest;
import org.adt.volunteerscase.dto.event.request.EventStatusPatchRequest;
import org.adt.volunteerscase.dto.event.response.GetAllResponse;
import org.adt.volunteerscase.dto.event.response.PatchResponse;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {

    void createEvent(EventCreateRequest request);
    PatchResponse patchEvent(Integer eventId, EventPatchRequest request);
    void updateStatus(Integer eventId, EventStatusPatchRequest request);
    void deleteEvent(Integer eventId);
    PageResponse<GetAllResponse> getAllEvents(Pageable pageable);

}

