package org.adt.volunteerscase.unit.service;

import org.adt.volunteerscase.dto.event.request.EventCreateRequest;
import org.adt.volunteerscase.dto.event.request.EventPatchRequest;
import org.adt.volunteerscase.dto.event.request.EventStatusPatchRequest;
import org.adt.volunteerscase.dto.event.response.GetAllResponse;
import org.adt.volunteerscase.dto.event.response.PatchResponse;
import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.adt.volunteerscase.entity.CoverEntity;
import org.adt.volunteerscase.entity.LocationEntity;
import org.adt.volunteerscase.entity.TagEntity;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.exception.CoverAlreadyExistsException;
import org.adt.volunteerscase.exception.EventNotFoundException;
import org.adt.volunteerscase.exception.LocationAlreadyExistsException;
import org.adt.volunteerscase.exception.SimultaneouslyCleaningAndWritingTagsException;
import org.adt.volunteerscase.repository.CoordinatorRepository;
import org.adt.volunteerscase.repository.CoverRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.repository.LocationRepository;
import org.adt.volunteerscase.service.EventService;
import org.adt.volunteerscase.service.TagService;
import org.adt.volunteerscase.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CoverRepository coverRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CoordinatorRepository coordinatorRepository;

    @Mock
    private TagService tagService;

    private EventService eventService;
    private LocalDateTime eventDate;
    private LocalDateTime updatedEventDate;
    private LocationEntity location;
    private CoverEntity cover;
    private CoordinatorEntity coordinator;
    private TagEntity firstTag;
    private EventCreateRequest createRequest;
    private EventEntity existingEvent;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(
                eventRepository,
                coverRepository,
                locationRepository,
                coordinatorRepository,
                tagService
        );

        eventDate = LocalDateTime.of(2026, 4, 20, 12, 0);
        updatedEventDate = LocalDateTime.of(2026, 4, 25, 15, 30);

        location = LocationEntity.builder()
                .locationId(10)
                .address("Moscow, Tverskaya 1")
                .latitude(55.7558)
                .longitude(37.6173)
                .build();

        cover = CoverEntity.builder()
                .coverId(5)
                .link("https://example.com/covers/cover-1.jpg")
                .width(1200)
                .height(630)
                .build();

        coordinator = CoordinatorEntity.builder()
                .userId(1)
                .workLocation("Main office")
                .build();

        firstTag = TagEntity.builder()
                .tagId(1)
                .tagName("animals")
                .build();

        createRequest = EventCreateRequest.builder()
                .name("City Cleanup")
                .status("ONGOING")
                .description("Cleaning the city park")
                .coverId(5)
                .coordinatorId(1)
                .maxCapacity(100)
                .dateTimestamp(eventDate)
                .locationId(10)
                .tagIds(Set.of(1))
                .build();

        existingEvent = EventEntity.builder()
                .eventId(1)
                .name("Old Event")
                .status(EventStatus.ONGOING)
                .description("Old description")
                .cover(cover)
                .coordinator(coordinator)
                .maxCapacity(50)
                .dateTimestamp(eventDate)
                .location(location)
                .tags(Set.of(firstTag))
                .build();
    }

    @Test
    void createEvent_shouldSaveEvent_whenRequestIsValid() {
        when(locationRepository.findByLocationId(10)).thenReturn(Optional.of(location));
        when(eventRepository.existsByLocationAndDateTimestamp(location, eventDate)).thenReturn(false);
        when(coverRepository.findByCoverId(5)).thenReturn(Optional.of(cover));
        when(eventRepository.existsByCover(cover)).thenReturn(false);
        when(coordinatorRepository.findById(1)).thenReturn(Optional.of(coordinator));
        when(tagService.getTagEntities(Set.of(1))).thenReturn(Set.of(firstTag));

        ArgumentCaptor<EventEntity> eventCaptor = ArgumentCaptor.forClass(EventEntity.class);

        eventService.createEvent(createRequest);

        verify(eventRepository).saveAndFlush(eventCaptor.capture());
        EventEntity savedEvent = eventCaptor.getValue();

        assertThat(savedEvent.getName()).isEqualTo("City Cleanup");
        assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.ONGOING);
        assertThat(savedEvent.getDescription()).isEqualTo("Cleaning the city park");
        assertThat(savedEvent.getCover()).isSameAs(cover);
        assertThat(savedEvent.getCoordinator()).isSameAs(coordinator);
        assertThat(savedEvent.getMaxCapacity()).isEqualTo(100);
        assertThat(savedEvent.getDateTimestamp()).isEqualTo(eventDate);
        assertThat(savedEvent.getLocation()).isSameAs(location);
        assertThat(savedEvent.getTags()).containsExactly(firstTag);

        verify(tagService).getTagEntities(Set.of(1));
    }

    @Test
    void createEvent_shouldThrowException_whenLocationIsOccupied() {
        when(locationRepository.findByLocationId(10)).thenReturn(Optional.of(location));
        when(eventRepository.existsByLocationAndDateTimestamp(location, eventDate)).thenReturn(true);

        assertThatThrownBy(() -> eventService.createEvent(createRequest))
                .isInstanceOf(LocationAlreadyExistsException.class)
                .hasMessage("location with id - 10 is occupied in date - " + eventDate);

        verify(locationRepository).findByLocationId(10);
        verify(eventRepository).existsByLocationAndDateTimestamp(location, eventDate);
        verify(eventRepository, never()).saveAndFlush(any(EventEntity.class));
        verifyNoInteractions(coverRepository, coordinatorRepository, tagService);
    }

    @Test
    void createEvent_shouldMapDatabaseConstraintViolation_whenCoverIsAlreadyUsed() {
        when(locationRepository.findByLocationId(10)).thenReturn(Optional.of(location));
        when(eventRepository.existsByLocationAndDateTimestamp(location, eventDate)).thenReturn(false);
        when(coverRepository.findByCoverId(5)).thenReturn(Optional.of(cover));
        when(eventRepository.existsByCover(cover)).thenReturn(false);
        when(coordinatorRepository.findById(1)).thenReturn(Optional.of(coordinator));
        when(tagService.getTagEntities(Set.of(1))).thenReturn(Set.of(firstTag));
        when(eventRepository.saveAndFlush(any(EventEntity.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "constraint violation",
                        new RuntimeException("uk_event_cover")
                ));

        assertThatThrownBy(() -> eventService.createEvent(createRequest))
                .isInstanceOf(CoverAlreadyExistsException.class)
                .hasMessage("cover with id - 5 is already used by another event");

        verify(eventRepository).saveAndFlush(any(EventEntity.class));
    }

    @Test
    void patchEvent_shouldUpdateFieldsAndReturnResponse() {
        CoordinatorEntity newCoordinator = CoordinatorEntity.builder()
                .userId(2)
                .workLocation("Branch office")
                .build();

        LocationEntity newLocation = LocationEntity.builder()
                .locationId(20)
                .address("Moscow, Arbat 12")
                .latitude(55.7522)
                .longitude(37.5927)
                .build();

        CoverEntity newCover = CoverEntity.builder()
                .coverId(30)
                .link("https://example.com/covers/new-cover.jpg")
                .width(1920)
                .height(1080)
                .build();

        TagEntity secondTag = TagEntity.builder()
                .tagId(2)
                .tagName("education")
                .build();

        EventPatchRequest request = EventPatchRequest.builder()
                .name("Updated Event")
                .description("Updated description")
                .eventStatus("COMPLETED")
                .coordinatorId(2)
                .maxCapacity(200)
                .dateTimestamp(updatedEventDate)
                .locationId(20)
                .coverId(30)
                .tagIds(Set.of(2))
                .build();

        when(eventRepository.findByEventId(1)).thenReturn(Optional.of(existingEvent));
        when(coordinatorRepository.findById(2)).thenReturn(Optional.of(newCoordinator));
        when(locationRepository.findByLocationId(20)).thenReturn(Optional.of(newLocation));
        when(eventRepository.existsByLocationAndDateTimestampAndEventIdNot(newLocation, updatedEventDate, 1))
                .thenReturn(false);
        when(coverRepository.findByCoverId(30)).thenReturn(Optional.of(newCover));
        when(eventRepository.existsByCoverAndEventIdNot(newCover, 1)).thenReturn(false);
        when(tagService.getTagEntities(Set.of(2))).thenReturn(Set.of(secondTag));
        when(eventRepository.saveAndFlush(any(EventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PatchResponse response = eventService.patchEvent(1, request);

        assertThat(existingEvent.getName()).isEqualTo("Updated Event");
        assertThat(existingEvent.getDescription()).isEqualTo("Updated description");
        assertThat(existingEvent.getStatus()).isEqualTo(EventStatus.COMPLETED);
        assertThat(existingEvent.getCoordinator()).isSameAs(newCoordinator);
        assertThat(existingEvent.getMaxCapacity()).isEqualTo(200);
        assertThat(existingEvent.getDateTimestamp()).isEqualTo(updatedEventDate);
        assertThat(existingEvent.getLocation()).isSameAs(newLocation);
        assertThat(existingEvent.getCover()).isSameAs(newCover);
        assertThat(existingEvent.getTags()).containsExactly(secondTag);

        assertThat(response.getEventId()).isEqualTo(1);
        assertThat(response.getName()).isEqualTo("Updated Event");
        assertThat(response.getDescription()).isEqualTo("Updated description");
        assertThat(response.getStatus()).isEqualTo(EventStatus.COMPLETED);
        assertThat(response.getCoverId()).isEqualTo(30);
        assertThat(response.getCoordinator().getUserId()).isEqualTo(2);
        assertThat(response.getCoordinator().getWorkLocation()).isEqualTo("Branch office");
        assertThat(response.getMaxCapacity()).isEqualTo(200);
        assertThat(response.getDateTimestamp()).isEqualTo(updatedEventDate);
        assertThat(response.getLocationId()).isEqualTo(20);
        assertThat(response.getLocationAddress()).isEqualTo("Moscow, Arbat 12");
        assertThat(response.getTagIds()).containsExactly(2);

        verify(tagService).getTagEntities(Set.of(2));
        verify(eventRepository).saveAndFlush(existingEvent);
    }

    @Test
    void patchEvent_shouldThrowException_whenEventNotFound() {
        when(eventRepository.findByEventId(77)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.patchEvent(77, EventPatchRequest.builder().build()))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("event with id - 77 not found");

        verify(eventRepository).findByEventId(77);
        verify(eventRepository, never()).saveAndFlush(any(EventEntity.class));
    }

    @Test
    void patchEvent_shouldThrowException_whenClearTagsAndTagIdsAreUsedTogether() {
        EventPatchRequest request = EventPatchRequest.builder()
                .clearTags(true)
                .tagIds(Set.of(1, 2))
                .build();

        when(eventRepository.findByEventId(1)).thenReturn(Optional.of(existingEvent));

        assertThatThrownBy(() -> eventService.patchEvent(1, request))
                .isInstanceOf(SimultaneouslyCleaningAndWritingTagsException.class)
                .hasMessage("clearTags and tagIds cannot be used together");

        verify(eventRepository).findByEventId(1);
        verify(tagService, never()).getTagEntities(anySet());
        verify(eventRepository, never()).saveAndFlush(any(EventEntity.class));
    }

    @Test
    void patchEvent_shouldThrowException_whenLocationIsOccupied() {
        EventPatchRequest request = EventPatchRequest.builder()
                .dateTimestamp(updatedEventDate)
                .build();

        when(eventRepository.findByEventId(1)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.existsByLocationAndDateTimestampAndEventIdNot(location, updatedEventDate, 1))
                .thenReturn(true);

        assertThatThrownBy(() -> eventService.patchEvent(1, request))
                .isInstanceOf(LocationAlreadyExistsException.class)
                .hasMessage("location with id - 10 is occupied in date - " + updatedEventDate);

        verify(eventRepository).findByEventId(1);
        verify(eventRepository).existsByLocationAndDateTimestampAndEventIdNot(location, updatedEventDate, 1);
        verify(eventRepository, never()).saveAndFlush(any(EventEntity.class));
    }

    @Test
    void updateStatus_shouldUpdateStatus_whenEventExists() {
        EventStatusPatchRequest request = EventStatusPatchRequest.builder()
                .eventStatus("COMPLETED")
                .build();

        when(eventRepository.findByEventId(1)).thenReturn(Optional.of(existingEvent));

        eventService.updateStatus(1, request);

        assertThat(existingEvent.getStatus()).isEqualTo(EventStatus.COMPLETED);
        verify(eventRepository).findByEventId(1);
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void updateStatus_shouldThrowException_whenEventNotFound() {
        EventStatusPatchRequest request = EventStatusPatchRequest.builder()
                .eventStatus("COMPLETED")
                .build();

        when(eventRepository.findByEventId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateStatus(99, request))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("event with id - 99 not found");

        verify(eventRepository).findByEventId(99);
        verify(eventRepository, never()).save(any(EventEntity.class));
    }

    @Test
    void deleteEvent_shouldDeleteById_whenEventExists() {
        when(eventRepository.existsById(1)).thenReturn(true);

        eventService.deleteEvent(1);

        verify(eventRepository).existsById(1);
        verify(eventRepository).deleteById(1);
    }

    @Test
    void deleteEvent_shouldThrowException_whenEventNotFound() {
        when(eventRepository.existsById(77)).thenReturn(false);

        assertThatThrownBy(() -> eventService.deleteEvent(77))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("event with id - 77 not found");

        verify(eventRepository).existsById(77);
        verify(eventRepository, never()).deleteById(anyInt());
    }

    @Test
    void getAllEvents_shouldReturnMappedPageResponse() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<EventEntity> eventPage = new PageImpl<>(List.of(existingEvent), pageable, 2);

        when(eventRepository.findAllByOrderByDateTimestampDesc(pageable)).thenReturn(eventPage);

        PageResponse<GetAllResponse> response = eventService.getAllEvents(pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPageNumber()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(1);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isFalse();

        GetAllResponse eventResponse = response.getContent().get(0);
        assertThat(eventResponse.getEventId()).isEqualTo(1);
        assertThat(eventResponse.getStatus()).isEqualTo(EventStatus.ONGOING);
        assertThat(eventResponse.getName()).isEqualTo("Old Event");
        assertThat(eventResponse.getDescription()).isEqualTo("Old description");
        assertThat(eventResponse.getCover().getCoverId()).isEqualTo(5);
        assertThat(eventResponse.getCover().getLink()).isEqualTo("https://example.com/covers/cover-1.jpg");
        assertThat(eventResponse.getCoordinator().getUserId()).isEqualTo(1);
        assertThat(eventResponse.getCoordinator().getWorkLocation()).isEqualTo("Main office");
        assertThat(eventResponse.getMaxCapacity()).isEqualTo(50);
        assertThat(eventResponse.getDateTimestamp()).isEqualTo(eventDate);
        assertThat(eventResponse.getLocation().getLocationId()).isEqualTo(10);
        assertThat(eventResponse.getLocation().getAddress()).isEqualTo("Moscow, Tverskaya 1");
        assertThat(eventResponse.getTags())
                .extracting("tagId", "tagName")
                .containsExactly(tuple(1, "animals"));

        verify(eventRepository).findAllByOrderByDateTimestampDesc(pageable);
    }
}
