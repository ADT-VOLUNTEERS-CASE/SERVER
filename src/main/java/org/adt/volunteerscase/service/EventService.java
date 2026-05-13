package org.adt.volunteerscase.service;


import org.adt.volunteerscase.dto.event.request.EventCreateRequest;
import org.adt.volunteerscase.dto.event.request.EventPatchRequest;
import org.adt.volunteerscase.dto.event.request.EventStatusPatchRequest;
import org.adt.volunteerscase.dto.event.response.GetAllResponse;
import org.adt.volunteerscase.dto.event.response.PatchResponse;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.entity.user.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.adt.volunteerscase.dto.event.request.EventSearchRequest;
import org.adt.volunteerscase.dto.event.request.EventCreateV2Request;
import org.adt.volunteerscase.dto.event.response.EventV2Response;

public interface EventService {

    void createEvent(EventCreateRequest request);
    PatchResponse patchEvent(Integer eventId, EventPatchRequest request);
    void updateStatus(Integer eventId, EventStatusPatchRequest request);
    void deleteEvent(Integer eventId);

    GetAllResponse getEventById(Integer eventId);
    PageResponse<GetAllResponse> getAllEvents(Pageable pageable);
    PageResponse<GetAllResponse> searchEvents(EventSearchRequest request, Pageable pageable);

    PageResponse<GetAllResponse> getRecommendations(Integer currentUserId, Pageable pageable);

    void createEventV2(EventCreateV2Request request);

    EventV2Response getEventV2ById(Integer eventId);

    PageResponse<EventV2Response> getAllEventsV2(Pageable pageable);
}
