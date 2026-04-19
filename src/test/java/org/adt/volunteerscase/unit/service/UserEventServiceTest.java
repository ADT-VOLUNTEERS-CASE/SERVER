package org.adt.volunteerscase.unit.service;

import org.adt.volunteerscase.dto.userEvent.request.UserEventStatusPatchRequest;
import org.adt.volunteerscase.dto.userEvent.response.UserEventResponse;
import org.adt.volunteerscase.entity.CoordinatorEntity;
import org.adt.volunteerscase.entity.UserEventEntity;
import org.adt.volunteerscase.entity.UserEventId;
import org.adt.volunteerscase.entity.event.EventEntity;
import org.adt.volunteerscase.entity.event.EventStatus;
import org.adt.volunteerscase.entity.user.UserEntity;
import org.adt.volunteerscase.exception.EventCapacityExceededException;
import org.adt.volunteerscase.exception.UserEventAccessDeniedException;
import org.adt.volunteerscase.exception.UserEventAlreadyExistsException;
import org.adt.volunteerscase.exception.UserEventStateConflictException;
import org.adt.volunteerscase.repository.EventRepository;
import org.adt.volunteerscase.repository.UserEventRepository;
import org.adt.volunteerscase.repository.UserRepository;
import org.adt.volunteerscase.service.UserEventService;
import org.adt.volunteerscase.service.impl.UserEventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventServiceTest {

    @Mock
    private UserEventRepository userEventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    private UserEventService userEventService;

    private UserEntity applicant;
    private CoordinatorEntity coordinator;
    private EventEntity event;
    private UserEventEntity pendingApplication;
    private UserEventEntity rejectedApplication;

    @BeforeEach
    void setUp() {
        userEventService = new UserEventServiceImpl(
                userEventRepository,
                userRepository,
                eventRepository
        );

        applicant = UserEntity.builder()
                .userId(10)
                .email("user@example.com")
                .build();

        coordinator = CoordinatorEntity.builder()
                .userId(99)
                .build();

        event = EventEntity.builder()
                .eventId(20)
                .name("Park Cleanup")
                .status(EventStatus.ONGOING)
                .dateTimestamp(LocalDateTime.now().plusDays(2))
                .maxCapacity(2)
                .coordinator(coordinator)
                .build();

        pendingApplication = UserEventEntity.builder()
                .id(UserEventId.builder()
                        .userId(10)
                        .eventId(20)
                        .build())
                .user(applicant)
                .event(event)
                .accepted(false)
                .rejected(false)
                .revoked(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        rejectedApplication = UserEventEntity.builder()
                .id(UserEventId.builder()
                        .userId(10)
                        .eventId(20)
                        .build())
                .user(applicant)
                .event(event)
                .accepted(false)
                .rejected(true)
                .revoked(false)
                .rejectReason("old reason")
                .createdAt(LocalDateTime.now().minusDays(3))
                .rejectedAt(LocalDateTime.now().minusDays(2))
                .build();
    }

    @Test
    void createApplication_shouldSavePendingApplication_whenEventHasCapacity() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userEventRepository.findByUserAndEvent(applicant, event)).thenReturn(Optional.empty());
        when(userEventRepository.countByEventAndDeletedAtIsNullAndRejectedFalseAndRevokedFalse(event))
                .thenReturn(1L);
        when(userEventRepository.save(any(UserEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEventResponse response = userEventService.createApplication(20, 10);

        assertThat(response.getUserId()).isEqualTo(10);
        assertThat(response.getEventId()).isEqualTo(20);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getRejectReason()).isNull();
        assertThat(response.getCreatedAt()).isNotNull();

        ArgumentCaptor<UserEventEntity> captor = ArgumentCaptor.forClass(UserEventEntity.class);
        verify(userEventRepository).save(captor.capture());

        UserEventEntity savedApplication = captor.getValue();
        assertThat(savedApplication.getId().getUserId()).isEqualTo(10);
        assertThat(savedApplication.getId().getEventId()).isEqualTo(20);
        assertThat(savedApplication.isAccepted()).isFalse();
        assertThat(savedApplication.isRejected()).isFalse();
        assertThat(savedApplication.isRevoked()).isFalse();
        assertThat(savedApplication.getRejectReason()).isNull();
        assertThat(savedApplication.getCreatedAt()).isNotNull();
    }

    @Test
    void createApplication_shouldThrowException_whenActiveApplicationAlreadyExists() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userEventRepository.findByUserAndEvent(applicant,
                event)).thenReturn(Optional.of(pendingApplication));

        assertThatThrownBy(() -> userEventService.createApplication(20, 10))
                .isInstanceOf(UserEventAlreadyExistsException.class)
                .hasMessage("active application for user id - 10 and event id - 20 already exists");

        verify(userEventRepository, never())
                .countByEventAndDeletedAtIsNullAndRejectedFalseAndRevokedFalse(event);
        verify(userEventRepository, never()).save(any(UserEventEntity.class));
    }

    @Test
    void createApplication_shouldReopenRejectedApplicationAsPending() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userEventRepository.findByUserAndEvent(applicant,
                event)).thenReturn(Optional.of(rejectedApplication));
        when(userEventRepository.countByEventAndDeletedAtIsNullAndRejectedFalseAndRevokedFalse(event))
                .thenReturn(1L);
        when(userEventRepository.save(any(UserEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEventResponse response = userEventService.createApplication(20, 10);

        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getRejectReason()).isNull();
        assertThat(response.getCreatedAt()).isNotNull();

        assertThat(rejectedApplication.isAccepted()).isFalse();
        assertThat(rejectedApplication.isRejected()).isFalse();
        assertThat(rejectedApplication.isRevoked()).isFalse();
        assertThat(rejectedApplication.getRejectReason()).isNull();
        assertThat(rejectedApplication.getRejectedAt()).isNull();

        verify(userEventRepository).save(rejectedApplication);
    }

    @Test
    void createApplication_shouldThrowException_whenCapacityIsReached() {
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userEventRepository.findByUserAndEvent(applicant, event)).thenReturn(Optional.empty());
        when(userEventRepository.countByEventAndDeletedAtIsNullAndRejectedFalseAndRevokedFalse(event))
                .thenReturn(2L);

        assertThatThrownBy(() -> userEventService.createApplication(20, 10))
                .isInstanceOf(EventCapacityExceededException.class)
                .hasMessage("event with id - 20 has reached max capacity - 2");

        verify(userEventRepository, never()).save(any(UserEventEntity.class));
    }

    @Test
    void updateApplicationStatus_shouldAcceptApplication_whenCurrentUserIsEventCoordinator() {
        UserEventStatusPatchRequest request = UserEventStatusPatchRequest.builder()
                .status("ACCEPTED")
                .build();

        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(userEventRepository.findByUserAndEventAndDeletedAtIsNull(applicant, event))
                .thenReturn(Optional.of(pendingApplication));
        when(userEventRepository.countByEventAndDeletedAtIsNullAndAcceptedTrueAndRevokedFalse(event))
                .thenReturn(0L);
        when(userEventRepository.save(any(UserEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEventResponse response = userEventService.updateApplicationStatus(20, 10, request, 99);

        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
        assertThat(response.getRejectReason()).isNull();
        assertThat(response.getRejectedAt()).isNull();
        assertThat(response.getRevokedAt()).isNull();

        assertThat(pendingApplication.isAccepted()).isTrue();
        assertThat(pendingApplication.isRejected()).isFalse();
        assertThat(pendingApplication.isRevoked()).isFalse();


        verify(userEventRepository).countByEventAndDeletedAtIsNullAndAcceptedTrueAndRevokedFalse(event);
        verify(userEventRepository).save(pendingApplication);
    }

    @Test
    void updateApplicationStatus_shouldRejectApplicationAndSetReason() {
        UserEventStatusPatchRequest request = UserEventStatusPatchRequest.builder()
                .status("REJECTED")
                .rejectReason("No available slots")
                .build();

        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(userEventRepository.findByUserAndEventAndDeletedAtIsNull(applicant, event))
                .thenReturn(Optional.of(pendingApplication));
        when(userEventRepository.save(any(UserEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEventResponse response = userEventService.updateApplicationStatus(20, 10, request, 99);

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        assertThat(response.getRejectReason()).isEqualTo("No available slots");
        assertThat(response.getRejectedAt()).isNotNull();

        assertThat(pendingApplication.isAccepted()).isFalse();
        assertThat(pendingApplication.isRejected()).isTrue();
        assertThat(pendingApplication.isRevoked()).isFalse();
        assertThat(pendingApplication.getRejectReason()).isEqualTo("No available slots");
        assertThat(pendingApplication.getRejectedAt()).isNotNull();

        verify(userEventRepository,
                never()).countByEventAndDeletedAtIsNullAndAcceptedTrueAndRevokedFalse(event);
        verify(userEventRepository).save(pendingApplication);
    }

    @Test
    void updateApplicationStatus_shouldThrowException_whenCurrentUserIsNotEventCoordinator() {
        UserEventStatusPatchRequest request = UserEventStatusPatchRequest.builder()
                .status("ACCEPTED")
                .build();

        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> userEventService.updateApplicationStatus(20, 10, request, 77))
                .isInstanceOf(UserEventAccessDeniedException.class)
                .hasMessage("user with id - 77 is not coordinator of event with id - 20");

        verify(userRepository, never()).findByUserIdAndDeletedAtIsNull(anyInt());
        verify(userEventRepository, never()).save(any(UserEventEntity.class));
    }

    @Test
    void updateApplicationStatus_shouldThrowException_whenAcceptedCapacityIsReached() {
        UserEventStatusPatchRequest request = UserEventStatusPatchRequest.builder()
                .status("ACCEPTED")
                .build();

        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(userEventRepository.findByUserAndEventAndDeletedAtIsNull(applicant, event))
                .thenReturn(Optional.of(pendingApplication));
        when(userEventRepository.countByEventAndDeletedAtIsNullAndAcceptedTrueAndRevokedFalse(event))
                .thenReturn(2L);

        assertThatThrownBy(() -> userEventService.updateApplicationStatus(20, 10, request, 99))
                .isInstanceOf(EventCapacityExceededException.class)
                .hasMessage("event with id - 20 has reached max capacity - 2");

        verify(userEventRepository, never()).save(any(UserEventEntity.class));
    }

    @Test
    void createApplication_shouldThrowException_whenEventDoesNotAcceptApplications() {
        event.setStatus(EventStatus.COMPLETED);

        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> userEventService.createApplication(20, 10))
                .isInstanceOf(UserEventStateConflictException.class)
                .hasMessage("event with id - 20 does not accept applications");

        verify(userEventRepository, never()).findByUserAndEvent(any(), any());
        verify(userEventRepository, never()).save(any(UserEventEntity.class));
    }

    @Test
    void updateApplicationStatus_shouldThrowException_whenApplicationIsRevoked() {
        pendingApplication.setRevoked(true);
        pendingApplication.setRevokedAt(LocalDateTime.now().minusHours(1));

        UserEventStatusPatchRequest request = UserEventStatusPatchRequest.builder()
                .status("ACCEPTED")
                .build();

        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(userEventRepository.findByUserAndEventAndDeletedAtIsNull(applicant, event))
                .thenReturn(Optional.of(pendingApplication));

        assertThatThrownBy(() -> userEventService.updateApplicationStatus(20, 10, request, 99))
                .isInstanceOf(UserEventStateConflictException.class)
                .hasMessage("application for user id - 10 and event id - 20 is revoked and cannot be updated");

        verify(userEventRepository, never()).countByEventAndDeletedAtIsNullAndRejectedFalseAndRevokedFalse(event);
        verify(userEventRepository, never()).countByEventAndDeletedAtIsNullAndAcceptedTrueAndRevokedFalse(event);
        verify(userEventRepository, never()).save(any(UserEventEntity.class));
    }

    @Test
    void updateApplicationStatus_shouldThrowException_whenStatusIsUnsupported() {
        UserEventStatusPatchRequest request = UserEventStatusPatchRequest.builder()
                .status("PENDING")
                .build();

        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(userEventRepository.findByUserAndEventAndDeletedAtIsNull(applicant, event))
                .thenReturn(Optional.of(pendingApplication));

        assertThatThrownBy(() -> userEventService.updateApplicationStatus(20, 10, request, 99))
                .isInstanceOf(UserEventStateConflictException.class)
                .hasMessage("unsupported application status - PENDING");

        verify(userEventRepository, never()).save(any(UserEventEntity.class));
    }

    @Test
    void updateApplicationStatus_shouldThrowException_whenEventDoesNotAcceptApplicationsForAccept() {
        event.setDateTimestamp(LocalDateTime.now().minusMinutes(1));

        UserEventStatusPatchRequest request = UserEventStatusPatchRequest.builder()
                .status("ACCEPTED")
                .build();

        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(userEventRepository.findByUserAndEventAndDeletedAtIsNull(applicant, event))
                .thenReturn(Optional.of(pendingApplication));

        assertThatThrownBy(() -> userEventService.updateApplicationStatus(20, 10, request, 99))
                .isInstanceOf(UserEventStateConflictException.class)
                .hasMessage("event with id - 20 does not accept applications");

        verify(userEventRepository, never()).countByEventAndDeletedAtIsNullAndRejectedFalseAndRevokedFalse(event);
        verify(userEventRepository, never()).countByEventAndDeletedAtIsNullAndAcceptedTrueAndRevokedFalse(event);
        verify(userEventRepository, never()).save(any(UserEventEntity.class));
    }

    @Test
    void updateApplicationStatus_shouldAcceptPreviouslyRejectedApplication_whenCapacityAllows() {
        UserEventStatusPatchRequest request = UserEventStatusPatchRequest.builder()
                .status("ACCEPTED")
                .build();

        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(userEventRepository.findByUserAndEventAndDeletedAtIsNull(applicant, event))
                .thenReturn(Optional.of(rejectedApplication));
        when(userEventRepository.countByEventAndDeletedAtIsNullAndRejectedFalseAndRevokedFalse(event))
                .thenReturn(1L);
        when(userEventRepository.countByEventAndDeletedAtIsNullAndAcceptedTrueAndRevokedFalse(event))
                .thenReturn(0L);
        when(userEventRepository.save(any(UserEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEventResponse response = userEventService.updateApplicationStatus(20, 10, request, 99);

        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
        assertThat(response.getRejectReason()).isNull();
        assertThat(response.getRejectedAt()).isNull();

        assertThat(rejectedApplication.isAccepted()).isTrue();
        assertThat(rejectedApplication.isRejected()).isFalse();
        assertThat(rejectedApplication.getRejectReason()).isNull();
        assertThat(rejectedApplication.getRejectedAt()).isNull();

        verify(userEventRepository).countByEventAndDeletedAtIsNullAndRejectedFalseAndRevokedFalse(event);
        verify(userEventRepository).countByEventAndDeletedAtIsNullAndAcceptedTrueAndRevokedFalse(event);
        verify(userEventRepository).save(rejectedApplication);
    }

    @Test
    void updateApplicationStatus_shouldRejectApplicationWithNullReason_whenReasonIsNotProvided() {
        UserEventStatusPatchRequest request = UserEventStatusPatchRequest.builder()
                .status("REJECTED")
                .build();

        when(eventRepository.findByEventIdForUpdate(20)).thenReturn(Optional.of(event));
        when(userRepository.findByUserIdAndDeletedAtIsNull(10)).thenReturn(Optional.of(applicant));
        when(userEventRepository.findByUserAndEventAndDeletedAtIsNull(applicant, event))
                .thenReturn(Optional.of(pendingApplication));
        when(userEventRepository.save(any(UserEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEventResponse response = userEventService.updateApplicationStatus(20, 10, request, 99);

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        assertThat(response.getRejectReason()).isNull();
        assertThat(response.getRejectedAt()).isNotNull();

        assertThat(pendingApplication.isAccepted()).isFalse();
        assertThat(pendingApplication.isRejected()).isTrue();
        assertThat(pendingApplication.getRejectReason()).isNull();

        verify(userEventRepository, never()).countByEventAndDeletedAtIsNullAndRejectedFalseAndRevokedFalse(event);
        verify(userEventRepository, never()).countByEventAndDeletedAtIsNullAndAcceptedTrueAndRevokedFalse(event);
        verify(userEventRepository).save(pendingApplication);
    }
}