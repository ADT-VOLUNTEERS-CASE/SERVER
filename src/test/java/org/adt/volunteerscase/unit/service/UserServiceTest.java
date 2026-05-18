package org.adt.volunteerscase.unit.service;

import org.adt.volunteerscase.dto.page.response.PageResponse;
import org.adt.volunteerscase.dto.tag.TagEntityDTO;
import org.adt.volunteerscase.dto.user.request.UpdateCoordinatorRequest;
import org.adt.volunteerscase.dto.user.response.GetUserResponse;
import org.adt.volunteerscase.dto.user.response.GetUserV2Response;
import org.adt.volunteerscase.dto.user.response.RegisteredEventResponse;
import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.adt.volunteerscase.entity.LocationEntity;
import org.adt.volunteerscase.entity.TagEntity;
import org.adt.volunteerscase.entity.UserEventEntity;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.entity.rating.RatingPeriod;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.CoordinatorInUseException;
import org.adt.volunteerscase.exception.CoordinatorNotFoundException;
import org.adt.volunteerscase.exception.UserAlreadyExistsException;
import org.adt.volunteerscase.exception.UserNotCoordinatorException;
import org.adt.volunteerscase.exception.UserNotFoundException;
import org.adt.volunteerscase.repository.CoordinatorRepository;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.repository.UserEventRepository;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.RatingService;
import org.adt.volunteerscase.service.UserService;
import org.adt.volunteerscase.service.impl.UserServiceImpl;
import org.adt.volunteerscase.service.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private CoordinatorRepository coordinatorRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserEventRepository userEventRepository;

    @Mock
    private RatingService ratingService;

    private UserService userService;
    private UserEntity coordinatorUser;
    private CoordinatorEntity coordinatorEntity;
    private UserEntity regularUser;
    private TagEntity userTag;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                refreshTokenService,
                coordinatorRepository,
                eventRepository,
                userEventRepository,
                ratingService
        );

        userTag = TagEntity.builder()
                .tagId(10)
                .tagName("animals")
                .build();

        coordinatorUser = UserEntity.builder()
                .userId(1)
                .firstname("Анна")
                .lastname("Координатор")
                .patronymic("Ивановна")
                .phoneNumber("+79990000001")
                .email("coordinator@example.com")
                .isCoordinator(true)
                .isAdmin(false)
                .tags(Set.of(userTag))
                .build();

        coordinatorEntity = CoordinatorEntity.builder()
                .userId(1)
                .user(coordinatorUser)
                .workLocation("Old office")
                .build();

        regularUser = UserEntity.builder()
                .userId(2)
                .firstname("Обычный")
                .lastname("Пользователь")
                .patronymic("Петрович")
                .phoneNumber("+79990000002")
                .email("user@example.com")
                .isCoordinator(false)
                .isAdmin(false)
                .build();
    }

    @Test
    void updateCoordinatorById_shouldUpdateFieldsAndReturnResponse() {
        UpdateCoordinatorRequest request = UpdateCoordinatorRequest.builder()
                .firstname("Мария")
                .lastname("Смирнова")
                .patronymic("Олеговна")
                .email("maria@example.com")
                .phoneNumber("+79990001122")
                .workLocation("Main office")
                .build();

        when(userRepository.findByUserIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(coordinatorUser));
        when(coordinatorRepository.findById(1)).thenReturn(Optional.of(coordinatorEntity));
        when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("+79990001122")).thenReturn(false);
        when(userEventRepository.findActiveUpcomingEventsByUserId(eq(1), any(LocalDateTime.class)))
                .thenReturn(List.of());

        GetUserResponse response = userService.updateCoordinatorById(request, 1);

        assertThat(coordinatorUser.getFirstname()).isEqualTo("Мария");
        assertThat(coordinatorUser.getLastname()).isEqualTo("Смирнова");
        assertThat(coordinatorUser.getPatronymic()).isEqualTo("Олеговна");
        assertThat(coordinatorUser.getEmail()).isEqualTo("maria@example.com");
        assertThat(coordinatorUser.getPhoneNumber()).isEqualTo("+79990001122");
        assertThat(coordinatorUser.getUpdatedAt()).isNotNull();
        assertThat(coordinatorEntity.getWorkLocation()).isEqualTo("Main office");

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getFirstname()).isEqualTo("Мария");
        assertThat(response.getLastname()).isEqualTo("Смирнова");
        assertThat(response.getPatronymic()).isEqualTo("Олеговна");
        assertThat(response.getEmail()).isEqualTo("maria@example.com");
        assertThat(response.getPhoneNumber()).isEqualTo("+79990001122");
        assertThat(response.isCoordinator()).isTrue();
        assertThat(response.isAdmin()).isFalse();
        assertThat(response.getWorkLocation()).isEqualTo("Main office");
        assertThat(response.getEvents()).isEmpty();
        assertThat(response.getTags()).containsExactlyInAnyOrder(
                TagEntityDTO.builder()
                        .tagId(10)
                        .tagName("animals")
                        .build()
        );

        verify(userRepository).findByUserIdAndDeletedAtIsNull(1);
        verify(coordinatorRepository).findById(1);
        verify(userRepository).existsByEmail("maria@example.com");
        verify(userRepository).existsByPhoneNumber("+79990001122");
        verify(userRepository).save(coordinatorUser);
        verify(coordinatorRepository).save(coordinatorEntity);
        verify(userEventRepository).findActiveUpcomingEventsByUserId(eq(1), any(LocalDateTime.class));
        verifyNoInteractions(eventRepository, refreshTokenService);
    }

    @Test
    void updateCoordinatorById_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateCoordinatorById(UpdateCoordinatorRequest.builder().build(), 99))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("user with id - 99 not found");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(99);
        verifyNoInteractions(coordinatorRepository, eventRepository, refreshTokenService, userEventRepository);
    }

    @Test
    void updateCoordinatorById_shouldThrowException_whenUserIsNotCoordinator() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(2)).thenReturn(Optional.of(regularUser));

        assertThatThrownBy(() -> userService.updateCoordinatorById(UpdateCoordinatorRequest.builder().build(), 2))
                .isInstanceOf(UserNotCoordinatorException.class)
                .hasMessage("The user that you want to change is not a coordinator");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(2);
        verify(coordinatorRepository, never()).findById(anyInt());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateCoordinatorById_shouldThrowException_whenCoordinatorRecordNotFound() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(coordinatorUser));
        when(coordinatorRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateCoordinatorById(UpdateCoordinatorRequest.builder().build(), 1))
                .isInstanceOf(CoordinatorNotFoundException.class)
                .hasMessage("coordinator with user id - 1 not found");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(1);
        verify(coordinatorRepository).findById(1);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateCoordinatorById_shouldThrowException_whenEmailAlreadyExists() {
        UpdateCoordinatorRequest request = UpdateCoordinatorRequest.builder()
                .email("taken@example.com")
                .build();

        when(userRepository.findByUserIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(coordinatorUser));
        when(coordinatorRepository.findById(1)).thenReturn(Optional.of(coordinatorEntity));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateCoordinatorById(request, 1))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("user with email - taken@example.com already exists");

        verify(userRepository).existsByEmail("taken@example.com");
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(coordinatorRepository, never()).save(any(CoordinatorEntity.class));
    }

    @Test
    void updateCoordinatorById_shouldThrowException_whenPhoneAlreadyExists() {
        UpdateCoordinatorRequest request = UpdateCoordinatorRequest.builder()
                .phoneNumber("+79995556677")
                .build();

        when(userRepository.findByUserIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(coordinatorUser));
        when(coordinatorRepository.findById(1)).thenReturn(Optional.of(coordinatorEntity));
        when(userRepository.existsByPhoneNumber("+79995556677")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateCoordinatorById(request, 1))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("user with phone number - +79995556677 already exists");

        verify(userRepository).existsByPhoneNumber("+79995556677");
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(coordinatorRepository, never()).save(any(CoordinatorEntity.class));
    }

    @Test
    void updateCoordinatorByEmail_shouldUpdateOnlyProvidedFields() {
        UpdateCoordinatorRequest request = UpdateCoordinatorRequest.builder()
                .lastname("Новая фамилия")
                .workLocation("Branch office")
                .build();

        when(userRepository.findByEmailAndDeletedAtIsNull("coordinator@example.com"))
                .thenReturn(Optional.of(coordinatorUser));
        when(coordinatorRepository.findById(1)).thenReturn(Optional.of(coordinatorEntity));
        when(userEventRepository.findActiveUpcomingEventsByUserId(eq(1), any(LocalDateTime.class)))
                .thenReturn(List.of());

        GetUserResponse response = userService.updateCoordinatorByEmail(request, "coordinator@example.com");

        assertThat(coordinatorUser.getFirstname()).isEqualTo("Анна");
        assertThat(coordinatorUser.getLastname()).isEqualTo("Новая фамилия");
        assertThat(coordinatorUser.getEmail()).isEqualTo("coordinator@example.com");
        assertThat(coordinatorEntity.getWorkLocation()).isEqualTo("Branch office");

        assertThat(response.getLastname()).isEqualTo("Новая фамилия");
        assertThat(response.getWorkLocation()).isEqualTo("Branch office");
        assertThat(response.getEvents()).isEmpty();

        verify(userRepository).findByEmailAndDeletedAtIsNull("coordinator@example.com");
        verify(coordinatorRepository).findById(1);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).existsByPhoneNumber(anyString());
        verify(userRepository).save(coordinatorUser);
        verify(coordinatorRepository).save(coordinatorEntity);
        verify(userEventRepository).findActiveUpcomingEventsByUserId(eq(1), any(LocalDateTime.class));
    }

    @Test
    void deleteCoordinatorById_shouldSoftDeleteUserAndActiveUserEvents() {
        UserEventEntity firstUserEvent = UserEventEntity.builder()
                .user(coordinatorUser)
                .build();

        UserEventEntity secondUserEvent = UserEventEntity.builder()
                .user(coordinatorUser)
                .build();

        List<UserEventEntity> activeUserEvents = List.of(firstUserEvent, secondUserEvent);

        when(userRepository.findByUserIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(coordinatorUser));
        when(coordinatorRepository.findById(1)).thenReturn(Optional.of(coordinatorEntity));
        when(eventRepository.existsByCoordinator(coordinatorEntity)).thenReturn(false);
        when(userEventRepository.findAllByUserAndDeletedAtIsNull(coordinatorUser)).thenReturn(activeUserEvents);

        userService.deleteCoordinatorById(1);

        assertThat(coordinatorUser.getDeletedAt()).isNotNull();
        assertThat(firstUserEvent.getDeletedAt()).isEqualTo(coordinatorUser.getDeletedAt());
        assertThat(secondUserEvent.getDeletedAt()).isEqualTo(coordinatorUser.getDeletedAt());

        InOrder inOrder = inOrder(
                userRepository,
                coordinatorRepository,
                eventRepository,
                refreshTokenService,
                userEventRepository
        );

        inOrder.verify(userRepository).findByUserIdAndDeletedAtIsNull(1);
        inOrder.verify(coordinatorRepository).findById(1);
        inOrder.verify(eventRepository).existsByCoordinator(coordinatorEntity);
        inOrder.verify(refreshTokenService).deleteAllByUser(coordinatorUser);
        inOrder.verify(coordinatorRepository).delete(coordinatorEntity);
        inOrder.verify(userEventRepository).findAllByUserAndDeletedAtIsNull(coordinatorUser);
        inOrder.verify(userEventRepository).saveAll(activeUserEvents);
        inOrder.verify(userRepository).save(coordinatorUser);
    }

    @Test
    void deleteCoordinatorById_shouldThrowException_whenCoordinatorIsAssignedToEvents() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(coordinatorUser));
        when(coordinatorRepository.findById(1)).thenReturn(Optional.of(coordinatorEntity));
        when(eventRepository.existsByCoordinator(coordinatorEntity)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteCoordinatorById(1))
                .isInstanceOf(CoordinatorInUseException.class)
                .hasMessage("coordinator with user id - 1 is assigned to one or more events");

        verify(refreshTokenService, never()).deleteAllByUser(any(UserEntity.class));
        verify(coordinatorRepository, never()).delete(any(CoordinatorEntity.class));
        verify(userEventRepository, never()).findAllByUserAndDeletedAtIsNull(any(UserEntity.class));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void deleteCoordinatorByEmail_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmailAndDeletedAtIsNull("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteCoordinatorByEmail("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("user with email - missing@example.com not found");

        verify(userRepository).findByEmailAndDeletedAtIsNull("missing@example.com");
        verifyNoInteractions(coordinatorRepository, eventRepository, refreshTokenService, userEventRepository);
    }

    @Test
    void getCurrentUser_shouldReturnCoordinatorResponseWithTags() {
        UserEntity currentUser = UserEntity.builder()
                .userId(1)
                .build();
        EventEntity activeUpcomingEvent = EventEntity.builder()
                .eventId(50)
                .name("Volunteer Meetup")
                .status(EventStatus.IN_PROGRESS)
                .maxCapacity(25)
                .dateTimestamp(LocalDateTime.of(2026, 5, 10, 18, 0))
                .build();

        when(userRepository.findByUserIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(coordinatorUser));
        when(coordinatorRepository.findById(1)).thenReturn(Optional.of(coordinatorEntity));
        when(userEventRepository.findActiveUpcomingEventsByUserId(eq(1), any(LocalDateTime.class)))
                .thenReturn(List.of(activeUpcomingEvent));

        GetUserResponse response = userService.getCurrentUser(currentUser);

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getFirstname()).isEqualTo("Анна");
        assertThat(response.getLastname()).isEqualTo("Координатор");
        assertThat(response.getPatronymic()).isEqualTo("Ивановна");
        assertThat(response.getEmail()).isEqualTo("coordinator@example.com");
        assertThat(response.getPhoneNumber()).isEqualTo("+79990000001");
        assertThat(response.isCoordinator()).isTrue();
        assertThat(response.isAdmin()).isFalse();
        assertThat(response.getWorkLocation()).isEqualTo("Old office");
        assertThat(response.getEvents()).hasSize(1);
        assertThat(response.getEvents().get(0).getEventId()).isEqualTo(50);
        assertThat(response.getEvents().get(0).getName()).isEqualTo("Volunteer Meetup");
        assertThat(response.getEvents().get(0).getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(response.getEvents().get(0).getMaxCapacity()).isEqualTo(25);
        assertThat(response.getTags()).containsExactlyInAnyOrder(
                TagEntityDTO.builder()
                        .tagId(10)
                        .tagName("animals")
                        .build()
        );

        verify(userRepository).findByUserIdAndDeletedAtIsNull(1);
        verify(coordinatorRepository).findById(1);
        verify(userEventRepository).findActiveUpcomingEventsByUserId(eq(1), any(LocalDateTime.class));
        verifyNoInteractions(ratingService);
    }

    @Test
    void getCurrentUser_shouldReturnRegularUserWithoutCoordinatorLookup() {
        UserEntity currentUser = UserEntity.builder()
                .userId(2)
                .build();

        when(userRepository.findByUserIdAndDeletedAtIsNull(2)).thenReturn(Optional.of(regularUser));
        when(userEventRepository.findActiveUpcomingEventsByUserId(eq(2), any(LocalDateTime.class)))
                .thenReturn(List.of());

        GetUserResponse response = userService.getCurrentUser(currentUser);

        assertThat(response.getId()).isEqualTo(2);
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.isCoordinator()).isFalse();
        assertThat(response.getWorkLocation()).isNull();
        assertThat(response.getTags()).isEmpty();
        assertThat(response.getEvents()).isEmpty();

        verify(userRepository).findByUserIdAndDeletedAtIsNull(2);
        verify(coordinatorRepository, never()).findById(anyInt());
        verify(userEventRepository).findActiveUpcomingEventsByUserId(eq(2), any(LocalDateTime.class));
        verifyNoInteractions(ratingService);
    }

    @Test
    void getCurrentUserV2_shouldReturnRegularUserWithRatings() {
        UserEntity currentUser = UserEntity.builder()
                .userId(2)
                .build();

        when(userRepository.findByUserIdAndDeletedAtIsNull(2)).thenReturn(Optional.of(regularUser));
        when(userEventRepository.findActiveUpcomingEventsByUserId(eq(2), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(ratingService.getUserRatingPosition(2, RatingPeriod.MONTHLY)).thenReturn(5);
        when(ratingService.getUserRatingPosition(2, RatingPeriod.OVERALL)).thenReturn(10);

        GetUserV2Response response = userService.getCurrentUserV2(currentUser);

        assertThat(response.getId()).isEqualTo(2);
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.isCoordinator()).isFalse();
        assertThat(response.getWorkLocation()).isNull();
        assertThat(response.getMonthlyRating()).isEqualTo(5);
        assertThat(response.getOverallRating()).isEqualTo(10);
        assertThat(response.getTags()).isEmpty();
        assertThat(response.getEvents()).isEmpty();

        verify(userRepository).findByUserIdAndDeletedAtIsNull(2);
        verify(coordinatorRepository, never()).findById(anyInt());
        verify(userEventRepository).findActiveUpcomingEventsByUserId(eq(2), any(LocalDateTime.class));
        verify(ratingService).getUserRatingPosition(2, RatingPeriod.MONTHLY);
        verify(ratingService).getUserRatingPosition(2, RatingPeriod.OVERALL);
    }

    @Test
    void getRegisteredEvents_shouldReturnPagedRegisteredEvents() {
        UserEntity currentUser = UserEntity.builder()
                .userId(2)
                .build();
        Pageable pageable = PageRequest.of(0, 4);

        UserEventEntity acceptedApplication = buildUserEvent(
                buildEvent(10, "Accepted event", EventStatus.COMPLETED),
                true,
                false,
                false
        );
        UserEventEntity rejectedApplication = buildUserEvent(
                buildEvent(11, "Rejected event", EventStatus.ONGOING),
                false,
                true,
                false
        );
        UserEventEntity revokedApplication = buildUserEvent(
                buildEvent(12, "Revoked event", EventStatus.IN_PROGRESS),
                false,
                false,
                true
        );
        UserEventEntity pendingApplication = buildUserEvent(
                buildEvent(13, "Pending event", EventStatus.ONGOING),
                false,
                false,
                false
        );

        when(userRepository.findByUserIdAndDeletedAtIsNull(2)).thenReturn(Optional.of(regularUser));
        when(userEventRepository.findRegisteredEventsByUserId(2, pageable))
                .thenReturn(new PageImpl<>(
                        List.of(acceptedApplication, rejectedApplication, revokedApplication, pendingApplication),
                        pageable,
                        4
                ));

        PageResponse<RegisteredEventResponse> response = userService.getRegisteredEvents(currentUser, pageable);

        assertThat(response.getContent()).hasSize(4);
        assertThat(response.getPageNumber()).isZero();
        assertThat(response.getPageSize()).isEqualTo(4);
        assertThat(response.getTotalElements()).isEqualTo(4);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();

        RegisteredEventResponse firstEvent = response.getContent().get(0);
        assertThat(firstEvent.getEventId()).isEqualTo(10);
        assertThat(firstEvent.getName()).isEqualTo("Accepted event");
        assertThat(firstEvent.getEventStatus()).isEqualTo("COMPLETED");
        assertThat(firstEvent.getApplicationStatus()).isEqualTo("ACCEPTED");
        assertThat(firstEvent.getMaxCapacity()).isEqualTo(20);
        assertThat(firstEvent.getWeightMinutes()).isEqualTo(90);
        assertThat(firstEvent.getLocationAddress()).isEqualTo("Москва, тестовая улица");
        assertThat(firstEvent.getCoordinatorId()).isEqualTo(1);
        assertThat(firstEvent.getCoordinatorFullName()).isEqualTo("Координатор Анна Ивановна");

        assertThat(response.getContent())
                .extracting(RegisteredEventResponse::getApplicationStatus)
                .containsExactly("ACCEPTED", "REJECTED", "REVOKED", "PENDING");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(2);
        verify(userEventRepository).findRegisteredEventsByUserId(2, pageable);
        verifyNoInteractions(ratingService);
    }

    @Test
    void getRegisteredEvents_shouldThrowException_whenUserNotFound() {
        UserEntity currentUser = UserEntity.builder()
                .userId(99)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUserIdAndDeletedAtIsNull(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getRegisteredEvents(currentUser, pageable))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("user with id - 99 not found");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(99);
        verify(userEventRepository, never()).findRegisteredEventsByUserId(anyInt(), any(Pageable.class));
    }

    private EventEntity buildEvent(Integer eventId, String name, EventStatus status) {
        LocationEntity location = LocationEntity.builder()
                .locationId(1)
                .address("Москва, тестовая улица")
                .build();

        return EventEntity.builder()
                .eventId(eventId)
                .name(name)
                .status(status)
                .maxCapacity(20)
                .weightMinutes(90)
                .dateTimestamp(LocalDateTime.of(2026, 5, 16, 14, 0))
                .location(location)
                .coordinator(coordinatorEntity)
                .build();
    }

    private UserEventEntity buildUserEvent(
            EventEntity event,
            boolean accepted,
            boolean rejected,
            boolean revoked
    ) {
        return UserEventEntity.builder()
                .user(regularUser)
                .event(event)
                .accepted(accepted)
                .rejected(rejected)
                .revoked(revoked)
                .build();
    }
}
