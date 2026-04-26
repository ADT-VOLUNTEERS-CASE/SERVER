package org.adt.volunteerscase.service.impl;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.dto.coordinator.CoordinatorEntityDTO;
import org.adt.volunteerscase.dto.cover.CoverMapper;
import org.adt.volunteerscase.dto.event.request.EventCreateRequest;
import org.adt.volunteerscase.dto.event.request.EventPatchRequest;
import org.adt.volunteerscase.dto.event.request.EventSearchRequest;
import org.adt.volunteerscase.dto.event.request.EventStatusPatchRequest;
import org.adt.volunteerscase.dto.event.response.GetAllResponse;
import org.adt.volunteerscase.dto.event.response.PatchResponse;
import org.adt.volunteerscase.dto.location.LocationEntityDTO;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.dto.tag.TagEntityDTO;
import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.adt.volunteerscase.entity.CoverEntity;
import org.adt.volunteerscase.entity.LocationEntity;
import org.adt.volunteerscase.entity.TagEntity;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.exception.*;
import org.adt.volunteerscase.repository.CoordinatorRepository;
import org.adt.volunteerscase.repository.CoverRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.repository.LocationRepository;
import org.adt.volunteerscase.service.EventService;
import org.adt.volunteerscase.service.TagService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CoverRepository coverRepository;
    private final LocationRepository locationRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final TagService tagService;
    private final CoverMapper coverMapper;


    @Override
    @Transactional
    public void createEvent(EventCreateRequest request) {

        LocationEntity locationEntity = null;
        if (request.getLocationId() != null) {
            locationEntity = locationRepository.findByLocationId(request.getLocationId())
                    .orElseThrow(() -> new LocationNotFoundException("location with id - " + request.getLocationId() + " not found"));
            if (eventRepository.existsByLocationAndDateTimestamp(locationEntity, request.getDateTimestamp())) {
                throw new LocationAlreadyExistsException(
                        "location with id - " + request.getLocationId()
                                + " is occupied in date - " + request.getDateTimestamp()
                );
            }
        }

        CoverEntity coverEntity = null;
        if (request.getCoverId() != null) {
            coverEntity = coverRepository.findByCoverIdAndDeletedAtIsNull(request.getCoverId())
                    .orElseThrow(() -> new CoverNotFoundException("cover with id - " +
                            request.getCoverId() + " not found"));
            if (eventRepository.existsByCover(coverEntity)) {
                throw new CoverAlreadyExistsException("cover with id - " + request.getCoverId() + " already exists");
            }
        }


        CoordinatorEntity coordinatorEntity = coordinatorRepository.findById(request.getCoordinatorId())
                .orElseThrow(() -> new CoordinatorNotFoundException(
                        "coordinator with id - " + request.getCoordinatorId() + " not found"));

        EventEntity eventEntity = EventEntity.builder()
                .name(request.getName())
                .status(EventStatus.valueOf(request.getStatus()))
                .description(request.getDescription())
                .cover(coverEntity)
                .coordinator(coordinatorEntity)
                .maxCapacity(request.getMaxCapacity())
                .dateTimestamp(request.getDateTimestamp())
                .location(locationEntity)
                .tags(tagService.getTagEntities(request.getTagIds()))
                .build();

        try {
            eventRepository.saveAndFlush(eventEntity);
        } catch (DataIntegrityViolationException ex) {
            throw mapEventConstraintException(ex, eventEntity);
        }

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

        if (request.getCoordinatorId() != null) {
            CoordinatorEntity coordinatorEntity = coordinatorRepository.findById(request.getCoordinatorId())
                    .orElseThrow(() -> new CoordinatorNotFoundException(
                            "coordinator with id - " + request.getCoordinatorId() + " not found"
                    ));
            event.setCoordinator(coordinatorEntity);
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
            event.setLocation(locationEntity);
        }

        if (request.getDateTimestamp() != null || request.getLocationId() != null) {
            if (event.getLocation() != null &&
                    eventRepository.existsByLocationAndDateTimestampAndEventIdNot(event.getLocation(), event.getDateTimestamp(), eventId)) {
                throw new LocationAlreadyExistsException(
                        "location with id - " + event.getLocation().getLocationId()
                                + " is occupied in date - " + event.getDateTimestamp()
                );
            }
        }

        if (Boolean.TRUE.equals(request.getClearCover()) && request.getCoverId() != null) {
            throw new SimultaneouslyCleaningAndWritingCoverException("clearCover and coverId cannot be used together");
        }

        if (Boolean.TRUE.equals(request.getClearCover())) {
            event.setCover(null);
        }

        if (request.getCoverId() != null) {
            CoverEntity coverEntity =
                    coverRepository.findByCoverIdAndDeletedAtIsNull(request.getCoverId())
                            .orElseThrow(() -> new CoverNotFoundException("cover with id - " +
                                    request.getCoverId() + " not found"));
            if (eventRepository.existsByCoverAndEventIdNot(coverEntity, eventId)) {
                throw new CoverAlreadyExistsException("cover with id - " + request.getCoverId() + " already exists");
            }
            event.setCover(coverEntity);
        }


        if (request.getEventStatus() != null) {
            event.setStatus(EventStatus.valueOf(request.getEventStatus()));
        }


        if (Boolean.TRUE.equals(request.getClearTags()) && request.getTagIds() != null) {
            throw new SimultaneouslyCleaningAndWritingTagsException("clearTags and tagIds cannot be used together");
        }

        if (Boolean.TRUE.equals(request.getClearTags())) {
            event.setTags(new HashSet<>());
        }

        if (request.getTagIds() != null) {
            Set<TagEntity> tags = tagService.getTagEntities(request.getTagIds());
            event.setTags(tags);
        }

        EventEntity updateEvent;
        try {
            updateEvent = eventRepository.saveAndFlush(event);
        } catch (DataIntegrityViolationException ex) {
            throw mapEventConstraintException(ex, event);
        }


        return PatchResponse.builder()
                .eventId(eventId)
                .name(updateEvent.getName())
                .description(updateEvent.getDescription())
                .status(updateEvent.getStatus())
                .cover(coverMapper.toDto(updateEvent.getCover()))
                .coordinator(convertCoordinatorToDTO(updateEvent.getCoordinator()))
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
    public GetAllResponse getEventById(Integer eventId) {
        EventEntity event = eventRepository.findByEventId(eventId)
                .orElseThrow(() -> new EventNotFoundException("event with id - " + eventId + " not found"));

        return convertToResponse(event);
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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GetAllResponse> searchEvents(EventSearchRequest request, Pageable pageable) {
        String escapedName = escapeLikePattern(request.getName());

        Page<EventEntity> eventPage = eventRepository.searchByName(escapedName, pageable);

        List<GetAllResponse> content = eventPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(new PageImpl<>(content, pageable, eventPage.getTotalElements()));
    }

    private String escapeLikePattern(String value) {
        return value.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    @Override
    public PageResponse<GetAllResponse> getRecommendations(Integer currentUserId, Pageable pageable) {

        Page<Integer> recommendedEventIdPage = eventRepository.findRecommendedEventIds(
                currentUserId,
                LocalDateTime.now(),
                pageable
        );

        if (recommendedEventIdPage.isEmpty()) {
            return PageResponse.of(
                    new PageImpl<>(
                            Collections.emptyList(),
                            recommendedEventIdPage.getPageable(),
                            recommendedEventIdPage.getTotalElements()
                    )
            );
        }

        List<Integer> rankedEventIds = recommendedEventIdPage.getContent();
        Map<Integer, Integer> eventOrder = new LinkedHashMap<>();

        for (int index = 0; index < rankedEventIds.size(); index++) {
            eventOrder.put(rankedEventIds.get(index), index);
        }

        List<GetAllResponse> content = eventRepository.findDetailedByEventIdIn(rankedEventIds).stream()
                .sorted(Comparator.comparingInt(
                        event -> eventOrder.getOrDefault(event.getEventId(), Integer.MAX_VALUE)
                ))
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(
                new PageImpl<>(
                        content,
                        recommendedEventIdPage.getPageable(),
                        recommendedEventIdPage.getTotalElements()
                )
        );
    }

    @Transactional(readOnly = true)
    private GetAllResponse convertToResponse(EventEntity event) {
        return GetAllResponse.builder()
                .eventId(event.getEventId())
                .status(event.getStatus())
                .name(event.getName())
                .description(event.getDescription())
                .cover(coverMapper.toDto(event.getCover()))
                .coordinator(convertCoordinatorToDTO(event.getCoordinator()))
                .maxCapacity(event.getMaxCapacity())
                .dateTimestamp(event.getDateTimestamp())
                .location(convertLocationToLocationDTO(event.getLocation()))
                .tags(convertTagsToTagsDTO(event.getTags()))
                .build();
    }

    @Transactional(readOnly = true)
    private Set<TagEntityDTO> convertTagsToTagsDTO(Set<TagEntity> tagEntities) {
        if (tagEntities == null || tagEntities.isEmpty()) {
            return Collections.emptySet();
        }

        return tagEntities.stream()
                .map(this::convertTagToDTO)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    private TagEntityDTO convertTagToDTO(TagEntity tag) {
        return TagEntityDTO.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName()).build();
    }

    @Transactional(readOnly = true)
    private LocationEntityDTO convertLocationToLocationDTO(LocationEntity location) {
        if (location == null) {
            return null;
        }
        return LocationEntityDTO.builder()
                .locationId(location.getLocationId())
                .address(location.getAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
    }

    @Transactional(readOnly = true)
    private CoordinatorEntityDTO convertCoordinatorToDTO(CoordinatorEntity coordinator) {
        if (coordinator == null) {
            return null;
        }

        return CoordinatorEntityDTO.builder()
                .userId(coordinator.getUserId())
                .workLocation(coordinator.getWorkLocation())
                .build();
    }


    private RuntimeException mapEventConstraintException(
            DataIntegrityViolationException ex,
            EventEntity event
    ) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (message != null && message.contains("uk_event_cover")) {
            String coverIdMessage = event.getCover() != null
                    ? " with id - " + event.getCover().getCoverId()
                    : "";

            return new CoverAlreadyExistsException(
                    "cover" + coverIdMessage + " is already used by another event"
            );
        }


        if (message != null && message.contains("uk_event_location_datetime")) {
            String locationIdMessage = event.getLocation() != null
                    ? " with id - " + event.getLocation().getLocationId()
                    : "";

            return new LocationAlreadyExistsException(
                    "location" + locationIdMessage + " is occupied in date - "
                            + event.getDateTimestamp()
            );
        }


        return ex;
    }


}
