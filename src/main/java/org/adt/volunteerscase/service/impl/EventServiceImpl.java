package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.event.request.EventCreateRequest;
import org.adt.volunteerscase.dto.event.request.EventPatchRequest;
import org.adt.volunteerscase.dto.event.request.EventStatusPatchRequest;
import org.adt.volunteerscase.dto.event.response.GetAllResponse;
import org.adt.volunteerscase.dto.event.response.PatchResponse;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.entity.CoverEntity;
import org.adt.volunteerscase.entity.LocationEntity;
import org.adt.volunteerscase.entity.TagEntity;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.exception.*;
import org.adt.volunteerscase.repository.CoverRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.repository.LocationRepository;
import org.adt.volunteerscase.service.EventService;
import org.adt.volunteerscase.service.TagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CoverRepository coverRepository;
    private final LocationRepository locationRepository;
    private final TagService tagService;

    @Override
    @Transactional
    public void createEvent(EventCreateRequest request) {

        LocationEntity locationEntity = null;
        if (request.getCoverId() != null) {
            locationEntity = locationRepository.findByLocationId(request.getLocationId())
                    .orElseThrow(() -> new LocationNotFoundException("location with id - " + request.getLocationId() + " not found"));

            if (eventRepository.existsByLocation(locationEntity)) {
                throw new LocationAlreadyExistsException("location with id - " + request.getLocationId() + " already exists");
            }
        }

        CoverEntity coverEntity = null;
        if (request.getCoverId() != null) {
            coverEntity = coverRepository.findByCoverId(request.getCoverId())
                    .orElseThrow(() -> new CoverNotFoundException("cover with id - " + request.getCoverId() + " not found"));
            if (eventRepository.existsByCover(coverEntity)) {
                throw new CoverAlreadyExistsException("cover with id - " + request.getCoverId() + " already exists");
            }
        }
        EventEntity eventEntity = EventEntity.builder()
                .name(request.getName())
                .status(EventStatus.valueOf(request.getStatus()))
                .description(request.getDescription())
                .cover(coverEntity)
                .coordinatorContact(request.getCoordinatorContact())
                .maxCapacity(request.getMaxCapacity())
                .dateTimestamp(request.getDateTimestamp())
                .location(locationEntity)
                .tags(tagService.getTagEntities(request.getTagIds()))
                .build();

        eventRepository.save(eventEntity);
    }

    @Override
    @Transactional
    public PatchResponse patchEvent(Integer eventId, EventPatchRequest request) {

        EventEntity event = eventRepository.findByEventId(eventId)
                .orElseThrow(() -> new EventNotFoundException("event with id - " + eventId + " not found"));

        if (request.getName() != null) {
            event.setName(request.getName());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getCoordinatorContact() != null) {
            event.setCoordinatorContact(request.getCoordinatorContact());
        }

        if (request.getMaxCapacity() != null) {
            event.setMaxCapacity(request.getMaxCapacity());
        }

        if (request.getDateTimestamp() != null) {
            event.setDateTimestamp(request.getDateTimestamp());
        }

        if (request.getLocationId() != null) {
            LocationEntity locationEntity = locationRepository.findByLocationId(request.getLocationId())
                    .orElseThrow(() -> new LocationNotFoundException("Location with id - " + request.getLocationId() + " not found"));
            if (eventRepository.existsByLocationAndEventIdNot(locationEntity, eventId)) {
                throw new LocationAlreadyExistsException("location with id - " + request.getLocationId() + " already exists");
            }
            event.setLocation(locationEntity);
        }

        if (request.getCoverId() != null) {
            CoverEntity coverEntity = coverRepository.findByCoverId(request.getCoverId())
                    .orElseThrow(() -> new CoverNotFoundException("cover with id - " + request.getCoverId() + " not found"));
            if (eventRepository.existsByCoverAndEventIdNot(coverEntity, eventId)) {
                throw new CoverAlreadyExistsException("cover with id - " + request.getCoverId() + " already exists");
            }
            event.setCover(coverEntity);
        }

        if (request.getEventStatus() != null) {
            event.setStatus(EventStatus.valueOf(request.getEventStatus()));
        }


        if (request.getTagIds() != null) {
            if (Boolean.TRUE.equals(request.getClearTags())) {
                event.getTags().clear();
            }

            Set<TagEntity> tags = tagService.getTagEntities(request.getTagIds());
            event.setTags(tags);
        }

        EventEntity updateEvent = eventRepository.save(event);

        return PatchResponse.builder()
                .eventId(eventId)
                .name(updateEvent.getName())
                .description(updateEvent.getDescription())
                .status(updateEvent.getStatus())
                .coverId(event.getCover() != null ? event.getCover().getCoverId() : null)
                .coordinatorContact(updateEvent.getCoordinatorContact())
                .maxCapacity(updateEvent.getMaxCapacity())
                .dateTimestamp(updateEvent.getDateTimestamp())
                .locationId(updateEvent.getLocation() != null ? updateEvent.getLocation().getLocationId() : null)
                .locationAddress(updateEvent.getLocation() != null ? updateEvent.getLocation().getAddress() : null)
                .tagIds(updateEvent.getTags() != null ?
                        updateEvent.getTags().stream()
                                .map(TagEntity::getTagId)
                                .collect(Collectors.toSet()) : null)
                .build();
    }

    @Override
    @Transactional
    public void updateStatus(Integer eventId, EventStatusPatchRequest request) {
        EventEntity event = eventRepository.findByEventId(eventId)
                .orElseThrow(() -> new EventNotFoundException("event with id - " + eventId + " not found"));
        event.setStatus(EventStatus.valueOf(request.getEventStatus()));
        eventRepository.save(event);
    }


    @Override
    @Transactional
    public void deleteEvent(Integer eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException("event with id - " + eventId + " not found");
        }
        eventRepository.deleteById(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GetAllResponse> getAllEvents(Pageable pageable) {
        Page<EventEntity> eventPage = eventRepository.findAllByOrderByDateTimestampDesc(pageable);

        List<GetAllResponse> content = eventPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return PageResponse.of(new PageImpl<>(content, pageable, eventPage.getTotalElements()));
    }

    @Transactional(readOnly = true)
    private GetAllResponse convertToResponse(EventEntity event) {
        return GetAllResponse.builder()
                .eventId(event.getEventId())
                .status(event.getStatus())
                .name(event.getName())
                .description(event.getDescription())
                .cover(event.getCover())
                .coordinatorContact(event.getCoordinatorContact())
                .maxCapacity(event.getMaxCapacity())
                .dateTimestamp(event.getDateTimestamp())
                .location(event.getLocation())
                .tags(event.getTags())
                .build();
    }

}
